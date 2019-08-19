import React, { Component } from 'react';
import { connect } from 'react-redux';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import LabeledField from 'field/LabeledField';
import Select, { components } from 'react-select';
import DescriptorOption from 'component/common/DescriptorOption';
import GeneralButton from 'field/input/GeneralButton';

const { Option, SingleValue } = components;

class TableSelectInput extends Component {
    constructor(props) {
        super(props);

        this.createTable = this.createTable.bind(this);
        this.createSelect = this.createSelect.bind(this);
        this.onRowSelected = this.onRowSelected.bind(this);
        this.onRowSelectedAll = this.onRowSelectedAll.bind(this);

        this.state = {
            showTable: false,
            data: [],
            selectedData: []
        };
    }

    onRowSelectedAll(isSelected, rows) {
        if (rows) {
            const selected = Object.assign([], this.state.selectedData);
            rows.forEach((row) => {
                this.createSelectedArray(selected, row, isSelected);
            });
            this.setState({
                selectedData: selected
            });
        } else {
            this.setState({
                selectedData: []
            });
        }
    }

    onRowSelected(row, isSelected) {
        const selected = Object.assign([], this.state.selectedData);
        this.createSelectedArray(selected, row, isSelected);
        this.setState({
            selectedData: selected
        });
    }

    createSelectedArray(selectedArray, row, isSelected) {
        if (isSelected) {
            const projectFound = selectedArray.find(project => project === row.name);
            if (!projectFound) {
                selectedArray.push(row.name);
            }
        } else {
            const index = selectedArray.indexOf(row.name);
            if (index >= 0) {
                selectedArray.splice(index, 1); // if found, remove that element from selected array
            }
        }
    }

    createRowSelectionProps() {
        const { readOnly } = this.props;

        if (readOnly) {
            return {};
        }

        return {
            mode: 'checkbox',
            clickToSelect: true,
            showOnlySelected: true,
            selected: this.state.selectedData,
            onSelect: this.onRowSelected,
            onSelectAll: this.onRowSelectedAll
        };
    }

    createTableData() {
        const { readOnly } = this.props;

        if (readOnly) {
            return this.state.data.filter(project => this.props.selectedData.find(selectedProject => project.name === selectedProject));
        }

        return this.state.data;
    }

    createTable() {
        const createCloseButton = (props) => {
            return (
                <div>
                    <div>
                        {props.components.pageList}
                    </div>
                    <div>
                        <span>Items per page</span>
                        {props.components.sizePerPageDropDown}
                    </div>
                    <div>
                        <GeneralButton onClick={() => this.setState({ showTable: false })}>OK</GeneralButton>
                    </div>
                </div>
            );
        };
        const tableOptions = {
            noDataText: 'No data found',
            clearSearch: true,
            defaultSortName: 'name',
            defaultSortOrder: 'asc',
            paginationPanel: createCloseButton
        };

        const projectsSelectRowProp = this.createRowSelectionProps();
        const tableData = this.createTableData();

        const assignDataFormat = (cell, row) => {
            const cellContent = (row.missing) ?
                <span className="missingBlackDuckData">
                    <span className="fa-layers fa-fw">
                        <FontAwesomeIcon icon="exclamation-triangle" className="alert-icon" size="lg" />{cell}
                    </span>
                </span>
                : cell;

            if (cell) {
                return <div title={cell.toString()}> {cellContent} </div>;
            }
            return <div> {cellContent} </div>;
        }

        return (<div>
            <BootstrapTable
                version="4"
                data={tableData}
                containerClass="table"
                hover
                condensed
                selectRow={projectsSelectRowProp}
                search
                options={tableOptions}
                trClassName="tableRow"
                headerContainerClass="scrollable"
                bodyContainerClass="tableScrollableBody"
            >
                <TableHeaderColumn dataField="name" isKey dataSort columnClassName="tableCell" dataFormat={assignDataFormat}>Project</TableHeaderColumn>
                <TableHeaderColumn dataField="description" dataSort columnClassName="tableCell" tdStyle={{ whiteSpace: 'normal' }} dataFormat={assignDataFormat}>Description</TableHeaderColumn>
                <TableHeaderColumn dataField="missing" dataFormat={assignDataFormat} hidden>Missing Data</TableHeaderColumn>
            </BootstrapTable>

            {this.props.fetching &&
            <div className="progressIcon">
                <span className="fa-layers fa-fw">
                    <FontAwesomeIcon icon="spinner" className="alert-icon" size="lg" spin />
                </span>
            </div>}

            {this.props.errorMsg && <p name="projectTableMessage">{this.props.errorMsg}</p>}
        </div>);
    }

    createSelect() {
        const typeOptionLabel = props => (
            <Option {...props}>
                <DescriptorOption icon={props.data.icon} label={props.data.label} value={props.data.value} />
            </Option>
        );

        const typeLabel = props => (
            <SingleValue {...props}>
                <DescriptorOption icon={props.data.icon} label={props.data.label} value={props.data.value} />
            </SingleValue>
        );

        const components = {
            Option: typeOptionLabel,
            SingleValue: typeLabel
        }

        return (
            <div>
                <Select
                    className="form-control"
                    onChange={null}
                    options={[]}
                    isMulti
                    components={components}
                    noOptionsMessage={null}
                    isDisabled
                />
                <button onClick={() => this.setState({ showTable: true })}>
                    Select
                </button>
            </div>
        );
    }

    render() {
        const selectField = (
            <div>
                <LabeledField field={this.createSelect()} {...this.props} />
            </div>
        );
        const tableField = this.createTable();

        return (
            <div>
                {selectField}
                {this.state.showTable && tableField}
            </div>
        );
    }
}

TableSelectInput.defaultProps = {};

TableSelectInput.propTypes = {};

const mapStateToProps = state => ({});

const mapDispatchToProps = dispatch => ({});

export default connect(mapStateToProps, mapDispatchToProps)(TableSelectInput);
