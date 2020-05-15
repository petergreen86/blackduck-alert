package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.ConfigGroupEntity;
import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorTypeEntity;
import com.synopsys.integration.alert.database.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.ConfigGroupRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;

public class DefaultConfigurationAccessorTest {
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TEST_SALT = "testSalt";
    private static final String TEST_DIRECTORY = "./testDB";
    private static final String TEST_SECRETS_DIRECTORY = "./testDB/run/secrets";
    private static final String FILE_NAME = "alert_encryption_data.json";
    private static final String SECRETS_RELATIVE_PATH = "../run/secrets/";
    private static final String ENCRYPTION_PASSWORD_FILE = SECRETS_RELATIVE_PATH + "ALERT_ENCRYPTION_PASSWORD";
    private static final String ENCRYPTION_SALT_FILE = SECRETS_RELATIVE_PATH + "ALERT_ENCRYPTION_GLOBAL_SALT";

    private AlertProperties alertProperties;
    private FilePersistenceUtil filePersistenceUtil;

    private ConfigGroupRepository configGroupRepository = Mockito.mock(ConfigGroupRepository.class);
    private DescriptorConfigRepository descriptorConfigRepository = Mockito.mock(DescriptorConfigRepository.class);
    private ConfigContextRepository configContextRepository = Mockito.mock(ConfigContextRepository.class);
    private FieldValueRepository fieldValueRepository = Mockito.mock(FieldValueRepository.class);
    private DefinedFieldRepository definedFieldRepository = Mockito.mock(DefinedFieldRepository.class);
    private RegisteredDescriptorRepository registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
    private DescriptorTypeRepository descriptorTypeRepository = Mockito.mock(DescriptorTypeRepository.class);
    private EncryptionUtility encryptionUtility = createEncryptionUtility();

    private final ConfigContextEnum configContextEnum = ConfigContextEnum.GLOBAL;
    private final String fieldValue = "testFieldValue";
    private final String fieldKey = "channel.common.name";

    @Test
    public void getAllJobsTest() {
        final Long jobId = 1L;
        UUID uuid = UUID.randomUUID();

        ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(jobId, uuid);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(2L, 2L, new Date(), new Date());
        descriptorConfigEntity.setId(3L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(3L, 4L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(4L);

        Mockito.when(configGroupRepository.findAll()).thenReturn(List.of(configGroupEntity));
        Mockito.when(descriptorConfigRepository.findById(Mockito.any())).thenReturn(Optional.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, definedFieldRepository, descriptorConfigRepository, configGroupRepository, configContextRepository, fieldValueRepository,
            encryptionUtility);
        List<ConfigurationJobModel> configurationJobModelList = configurationAccessor.getAllJobs();

        assertEquals(1, configurationJobModelList.size());
        ConfigurationJobModel configurationJobModel = configurationJobModelList.get(0);
        assertEquals(uuid, configurationJobModel.getJobId());
        assertEquals(fieldValue, configurationJobModel.getName());
    }

    @Test
    public void getJobByIdTest() throws Exception {
        UUID jobId = UUID.randomUUID();

        ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(1L, jobId);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(2L, 2L, new Date(), new Date());
        descriptorConfigEntity.setId(3L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(3L, 4L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(4L);

        Mockito.when(configGroupRepository.findByJobId(Mockito.any())).thenReturn(List.of(configGroupEntity));
        Mockito.when(descriptorConfigRepository.findById(Mockito.any())).thenReturn(Optional.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, definedFieldRepository, descriptorConfigRepository, configGroupRepository, configContextRepository, fieldValueRepository,
            encryptionUtility);
        Optional<ConfigurationJobModel> configurationJobModelOptional = configurationAccessor.getJobById(jobId);

        assertTrue(configurationJobModelOptional.isPresent());
        ConfigurationJobModel configurationJobModel = configurationJobModelOptional.get();
        assertEquals(jobId, configurationJobModel.getJobId());
        assertEquals(fieldValue, configurationJobModel.getName());
    }

    @Test
    public void getJobsByFrequency() {
        FrequencyType frequencyType = FrequencyType.DAILY;
        final Long jobId = 1L;
        UUID uuid = UUID.randomUUID();
        final String fieldValue = "DAILY";
        final String fieldKey = "channel.common.frequency";

        ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(jobId, uuid);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(2L, 2L, new Date(), new Date());
        descriptorConfigEntity.setId(3L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(3L, 4L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(4L);

        Mockito.when(configGroupRepository.findAll()).thenReturn(List.of(configGroupEntity));
        Mockito.when(descriptorConfigRepository.findById(Mockito.any())).thenReturn(Optional.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, definedFieldRepository, descriptorConfigRepository, configGroupRepository, configContextRepository, fieldValueRepository,
            encryptionUtility);
        List<ConfigurationJobModel> configurationJobModelList = configurationAccessor.getJobsByFrequency(frequencyType);

        assertEquals(1, configurationJobModelList.size());
        ConfigurationJobModel configurationJobModel = configurationJobModelList.get(0);
        assertEquals(uuid, configurationJobModel.getJobId());
        assertEquals(fieldValue, configurationJobModel.getFrequencyType().toString());
    }

    @Test
    public void getJobByIdNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.getJobById(null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void createJobTest() throws Exception {
        List<String> descriptorNames = List.of("descriptor-name-test");

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create("channel.common.name");
        configurationFieldModel.setFieldValue(fieldValue);
        List<ConfigurationFieldModel> configuredFields = List.of(configurationFieldModel);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name", 1L);
        registeredDescriptorEntity.setId(2L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());
        configContextEntity.setId(3L);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(4L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(5L, 6L, new Date(), new Date());
        descriptorConfigEntity.setId(5L);

        Mockito.when(registeredDescriptorRepository.findFirstByName(Mockito.any())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(configContextRepository.findFirstByContext(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(definedFieldRepository.findByDescriptorIdAndContext(Mockito.any(), Mockito.any())).thenReturn(List.of(definedFieldEntity));
        Mockito.when(descriptorConfigRepository.save(Mockito.any())).thenReturn(descriptorConfigEntity);
        Mockito.when(definedFieldRepository.findFirstByKey(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, null, definedFieldRepository, descriptorConfigRepository, configGroupRepository, configContextRepository,
            fieldValueRepository, null);
        ConfigurationJobModel configurationJobModel = configurationAccessor.createJob(descriptorNames, configuredFields);

        assertEquals(fieldValue, configurationJobModel.getName());
    }

    @Test
    public void updateJobTest() throws Exception {
        UUID uuid = UUID.randomUUID();
        List<String> descriptorNames = List.of("descriptor-name-test");

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create("channel.common.name");
        configurationFieldModel.setFieldValue(fieldValue);
        List<ConfigurationFieldModel> configuredFields = List.of(configurationFieldModel);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name", 1L);
        registeredDescriptorEntity.setId(2L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());
        configContextEntity.setId(3L);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(4L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(5L, 6L, new Date(), new Date());
        descriptorConfigEntity.setId(5L);
        ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(6L, uuid);

        Mockito.when(registeredDescriptorRepository.findFirstByName(Mockito.any())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(configContextRepository.findFirstByContext(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(definedFieldRepository.findByDescriptorIdAndContext(Mockito.any(), Mockito.any())).thenReturn(List.of(definedFieldEntity));
        Mockito.when(descriptorConfigRepository.save(Mockito.any())).thenReturn(descriptorConfigEntity);
        Mockito.when(definedFieldRepository.findFirstByKey(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        Mockito.when(configGroupRepository.findByJobId(Mockito.any())).thenReturn(List.of(configGroupEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, null, definedFieldRepository, descriptorConfigRepository, configGroupRepository, configContextRepository,
            fieldValueRepository, null);
        ConfigurationJobModel configurationJobModel = configurationAccessor.updateJob(uuid, descriptorNames, configuredFields);

        Mockito.verify(descriptorConfigRepository).deleteById(Mockito.any());
        assertEquals(uuid, configurationJobModel.getJobId());
        assertEquals(fieldValue, configurationJobModel.getName());
    }

    @Test
    public void updateJobNullIdTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.updateJob(null, null, null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void deleteJobNullIdTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.deleteJob(null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getProviderConfigurationByNameTest() throws Exception {
        final String providerConfigName = "provider-config-name-test";
        final String emptyProviderConfigName = "bad-config-name";
        final Long fieldId = 1L;
        final Long descriptorId = 4L;
        final Long configurationId = 6L;

        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(fieldId);
        FieldValueEntity fieldValueEntity = new FieldValueEntity(2L, 3L, fieldValue);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 5L, new Date(), new Date());
        descriptorConfigEntity.setId(configurationId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());

        Mockito.when(definedFieldRepository.findFirstByKey(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));
        Mockito.when(fieldValueRepository.findAllByFieldIdAndValue(fieldId, providerConfigName)).thenReturn(List.of(fieldValueEntity));
        Mockito.when(fieldValueRepository.findAllByFieldIdAndValue(fieldId, emptyProviderConfigName)).thenReturn(List.of());
        Mockito.when(descriptorConfigRepository.findById(Mockito.any())).thenReturn(Optional.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, definedFieldRepository, descriptorConfigRepository, null, configContextRepository, fieldValueRepository, encryptionUtility);
        Optional<ConfigurationModel> configurationModelOptional = configurationAccessor.getProviderConfigurationByName(providerConfigName);
        Optional<ConfigurationModel> configurationModelProviderConfigsEmpty = configurationAccessor.getProviderConfigurationByName(emptyProviderConfigName);

        assertTrue(configurationModelOptional.isPresent());
        assertFalse(configurationModelProviderConfigsEmpty.isPresent());

        ConfigurationModel configurationModel = configurationModelOptional.get();
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getProviderConfigurationByNameBlankTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.getProviderConfigurationByName("");
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getConfigurationByIdEmptyTest() throws Exception {
        Mockito.when(descriptorConfigRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, descriptorConfigRepository, null, null, null, null);
        Optional<ConfigurationModel> configurationModelOptional = configurationAccessor.getConfigurationById(1L);

        assertFalse(configurationModelOptional.isPresent());
    }

    @Test
    public void getConfigurationByIdNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.getConfigurationById(null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getConfigurationsByDescriptorKeyTest() throws Exception {
        final Long descriptorId = 3L;
        final Long configurationId = 5L;

        DescriptorKey descriptorKey = createDescriptorKey("descriptorKeyName");
        DescriptorKey badDescriptorKey = createDescriptorKey("bad-descriptorKey");
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name-test", 1L);
        registeredDescriptorEntity.setId(2L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 4L, new Date(), new Date());
        descriptorConfigEntity.setId(configurationId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(6L, 7L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(8L);

        Mockito.when(registeredDescriptorRepository.findFirstByName(descriptorKey.getUniversalKey())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(registeredDescriptorRepository.findFirstByName(badDescriptorKey.getUniversalKey())).thenReturn(Optional.empty());
        Mockito.when(descriptorConfigRepository.findByDescriptorId(Mockito.any())).thenReturn(List.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, null, definedFieldRepository, descriptorConfigRepository, null, configContextRepository, fieldValueRepository,
            encryptionUtility);
        List<ConfigurationModel> configurationModelList = configurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
        List<ConfigurationModel> configurationModelListEmpty = configurationAccessor.getConfigurationsByDescriptorKey(badDescriptorKey);

        assertEquals(1, configurationModelList.size());
        assertTrue(configurationModelListEmpty.isEmpty());
        ConfigurationModel configurationModel = configurationModelList.get(0);
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getConfigurationsByDescriptorKeyNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.getConfigurationsByDescriptorKey(null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getConfigurationsByDescriptorTypeTest() throws Exception {
        final Long descriptorId = 3L;
        final Long configurationId = 5L;
        DescriptorType descriptorType = DescriptorType.CHANNEL;

        DescriptorTypeEntity descriptorTypeEntity = new DescriptorTypeEntity("CHANNEL");
        descriptorTypeEntity.setId(1L);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name-test", 1L);
        registeredDescriptorEntity.setId(2L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 4L, new Date(), new Date());
        descriptorConfigEntity.setId(configurationId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(6L, 7L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(8L);

        Mockito.when(descriptorTypeRepository.findFirstByType(Mockito.any())).thenReturn(Optional.of(descriptorTypeEntity));
        Mockito.when(registeredDescriptorRepository.findByTypeId(Mockito.any())).thenReturn(List.of(registeredDescriptorEntity));
        Mockito.when(descriptorConfigRepository.findByDescriptorId(Mockito.any())).thenReturn(List.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, descriptorTypeRepository, definedFieldRepository, descriptorConfigRepository, null, configContextRepository,
            fieldValueRepository,
            encryptionUtility);
        List<ConfigurationModel> configurationModelList = configurationAccessor.getConfigurationsByDescriptorType(descriptorType);

        assertEquals(1, configurationModelList.size());
        ConfigurationModel configurationModel = configurationModelList.get(0);
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getConfigurationsByDescriptorTypeNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.getConfigurationsByDescriptorType(null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getChannelConfigurationsByFrequencyTest() throws Exception {
        final Long descriptorId = 3L;
        final Long configurationId = 5L;
        FrequencyType frequencyType = FrequencyType.DAILY;

        DescriptorTypeEntity descriptorTypeEntity = new DescriptorTypeEntity("CHANNEL");
        descriptorTypeEntity.setId(1L);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name-test", 1L);
        registeredDescriptorEntity.setId(2L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 4L, new Date(), new Date());
        descriptorConfigEntity.setId(configurationId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(6L, 7L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(8L);

        Mockito.when(descriptorTypeRepository.findFirstByType(Mockito.any())).thenReturn(Optional.of(descriptorTypeEntity));
        Mockito.when(registeredDescriptorRepository.findByTypeIdAndFrequency(Mockito.any(), Mockito.any())).thenReturn(List.of(registeredDescriptorEntity));
        Mockito.when(descriptorConfigRepository.findByDescriptorId(Mockito.any())).thenReturn(List.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, descriptorTypeRepository, definedFieldRepository, descriptorConfigRepository, null, configContextRepository,
            fieldValueRepository,
            encryptionUtility);
        List<ConfigurationModel> configurationModelList = configurationAccessor.getChannelConfigurationsByFrequency(frequencyType);

        assertEquals(1, configurationModelList.size());
        ConfigurationModel configurationModel = configurationModelList.get(0);
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getChannelConfigurationsByFrequencyNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.getChannelConfigurationsByFrequency(null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getConfigurationsByDescriptorKeyAndContextTest() throws Exception {
        final Long descriptorId = 3L;
        final Long configurationId = 5L;

        DescriptorKey descriptorKey = createDescriptorKey("descriptorKeyName");
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 4L, new Date(), new Date());
        descriptorConfigEntity.setId(configurationId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());
        configContextEntity.setId(3L);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name", 1L);
        registeredDescriptorEntity.setId(2L);
        FieldValueEntity fieldValueEntity = new FieldValueEntity(6L, 7L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(8L);

        Mockito.when(descriptorConfigRepository.findByDescriptorIdAndContextId(Mockito.any(), Mockito.any())).thenReturn(List.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findFirstByContext(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(registeredDescriptorRepository.findFirstByName(Mockito.any())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));
        EncryptionUtility encryptionUtility = createEncryptionUtility();

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, null, definedFieldRepository, descriptorConfigRepository, null, configContextRepository,
            fieldValueRepository,
            encryptionUtility);
        List<ConfigurationModel> configurationModelList = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey, configContextEnum);

        assertEquals(1, configurationModelList.size());
        ConfigurationModel configurationModel = configurationModelList.get(0);
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getConfigurationsByDescriptorKeyAndContextNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.getConfigurationsByDescriptorKeyAndContext(null, null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void createConfigurationTest() throws Exception {
        final Long descriptorId = 3L;
        final Long configurationId = 5L;

        DescriptorKey descriptorKey = createDescriptorKey("descriptorKeyName");
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create("channel.common.name");
        configurationFieldModel.setFieldValue(fieldValue);
        List<ConfigurationFieldModel> configuredFields = List.of(configurationFieldModel);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name", 1L);
        registeredDescriptorEntity.setId(descriptorId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());
        configContextEntity.setId(3L);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(4L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(5L, 6L, new Date(), new Date());
        descriptorConfigEntity.setId(configurationId);

        Mockito.when(registeredDescriptorRepository.findFirstByName(Mockito.any())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(configContextRepository.findFirstByContext(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(definedFieldRepository.findByDescriptorIdAndContext(Mockito.any(), Mockito.any())).thenReturn(List.of(definedFieldEntity));
        Mockito.when(descriptorConfigRepository.save(Mockito.any())).thenReturn(descriptorConfigEntity);
        Mockito.when(definedFieldRepository.findFirstByKey(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, null, definedFieldRepository, descriptorConfigRepository, configGroupRepository, configContextRepository,
            fieldValueRepository, null);
        ConfigurationModel configurationModel = configurationAccessor.createConfiguration(descriptorKey, configContextEnum, configuredFields);

        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void createConfigurationNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.createConfiguration(null, null, null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getConfigurationByDescriptorNameAndContextNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.getConfigurationsByDescriptorNameAndContext(null, configContextEnum);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }

        try {
            configurationAccessor.getConfigurationsByDescriptorNameAndContext("descriptorName", null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void updateConfigurationTest() throws Exception {
        Long configurationId = 2L;
        Long descriptorId = 3L;

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create("channel.common.name");
        configurationFieldModel.setFieldValue(fieldValue);
        List<ConfigurationFieldModel> configuredFields = List.of(configurationFieldModel);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 4L, new Date(), new Date());
        descriptorConfigEntity.setId(configurationId);
        FieldValueEntity fieldValueEntity = new FieldValueEntity(5L, 6L, fieldValue);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.toString());
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(7L);

        Mockito.when(descriptorConfigRepository.findById(Mockito.any())).thenReturn(Optional.of(descriptorConfigEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(definedFieldRepository.findFirstByKey(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, definedFieldRepository, descriptorConfigRepository, null, configContextRepository,
            fieldValueRepository, null);
        ConfigurationModel configurationModel = configurationAccessor.updateConfiguration(1L, configuredFields);

        Mockito.verify(fieldValueRepository).deleteAll(Mockito.any());
        Mockito.verify(descriptorConfigRepository).save(Mockito.any());

        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void updateConfigurationNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            configurationAccessor.updateConfiguration(null, null);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void deleteConfigurationTest() throws Exception {
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 2L, "dateCreated", "lastUpdated", configContextEnum);

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, descriptorConfigRepository, null, null, null, null);
        configurationAccessor.deleteConfiguration(configurationModel);

        Mockito.verify(descriptorConfigRepository).deleteById(Mockito.any());
    }

    @Test
    public void deleteConfigurationNullConfigModelTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            ConfigurationModel configurationModel = null;
            configurationAccessor.deleteConfiguration(configurationModel);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void deleteConfigurationNullDescriptorConfigIdTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null, null);
        try {
            Long descriptorConfigId = null;
            configurationAccessor.deleteConfiguration(descriptorConfigId);
            fail();
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    private void testConfigurationModel(Long configurationId, Long descriptorId, ConfigurationModel configurationModel) {
        assertEquals(configurationId, configurationModel.getConfigurationId());
        assertEquals(descriptorId, configurationModel.getDescriptorId());
        assertEquals(configContextEnum, configurationModel.getDescriptorContext());
    }

    private EncryptionUtility createEncryptionUtility() {
        alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.of(TEST_SALT));
        Mockito.when(alertProperties.getAlertConfigHome()).thenReturn(TEST_DIRECTORY);
        Mockito.when(alertProperties.getAlertSecretsDir()).thenReturn(TEST_SECRETS_DIRECTORY);
        filePersistenceUtil = new FilePersistenceUtil(alertProperties, new Gson());
        return new EncryptionUtility(alertProperties, filePersistenceUtil);
    }

    private DescriptorKey createDescriptorKey(String key) {
        DescriptorKey testDescriptorKey = new DescriptorKey() {
            private static final long serialVersionUID = -8807612002958685145L;

            @Override
            public String getUniversalKey() {
                return key;
            }

            @Override
            public String getDisplayName() {
                return key;
            }
        };
        return testDescriptorKey;
    }
}
