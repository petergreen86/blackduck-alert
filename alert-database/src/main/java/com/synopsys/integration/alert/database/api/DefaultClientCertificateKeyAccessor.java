package com.synopsys.integration.alert.database.api;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.persistence.accessor.ClientCertificateKeyAccessor;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateKeyModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.certificates.ClientCertificateKeyEntity;
import com.synopsys.integration.alert.database.certificates.ClientCertificateKeyRepository;

public class DefaultClientCertificateKeyAccessor implements ClientCertificateKeyAccessor {
    private final EncryptionUtility encryptionUtility;

    private final ClientCertificateKeyRepository clientCertificateKeyRepository;

    public DefaultClientCertificateKeyAccessor(EncryptionUtility encryptionUtility, ClientCertificateKeyRepository clientCertificateKeyRepository) {
        this.encryptionUtility = encryptionUtility;
        this.clientCertificateKeyRepository = clientCertificateKeyRepository;
    }

    @Override
    public Optional<ClientCertificateKeyModel> getCertificateKey(UUID id) {
        return clientCertificateKeyRepository.findById(id).map(this::toModel);
    }

    @Override
    public ClientCertificateKeyModel saveCertificateKey(ClientCertificateKeyModel certificateKeyModel) {
        ClientCertificateKeyEntity entityToSave = toEntity(certificateKeyModel, DateUtils.createCurrentDateTimestamp());
        ClientCertificateKeyEntity updatedEntity = clientCertificateKeyRepository.save(entityToSave);

        return toModel(updatedEntity);
    }

    @Override
    public void deleteCertificateKey(UUID certificateKeyId) {
        clientCertificateKeyRepository.deleteById(certificateKeyId);
    }

    private ClientCertificateKeyEntity toEntity(ClientCertificateKeyModel model, OffsetDateTime lastUpdated) {
        String password = model.getPassword().map(encryptionUtility::encrypt).orElse(null);

        return new ClientCertificateKeyEntity(model.getId(), model.getName(), password, model.getKeyContent(), lastUpdated);
    }

    private ClientCertificateKeyModel toModel(ClientCertificateKeyEntity entity) {
        String password = entity.getPassword();
        boolean doesPasswordExist = StringUtils.isNotBlank(password);
        if (doesPasswordExist) {
            password = encryptionUtility.decrypt(password);
        }

        return new ClientCertificateKeyModel(entity.getId(), entity.getName(), password, doesPasswordExist, entity.getKeyContent(),
                DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
    }
}
