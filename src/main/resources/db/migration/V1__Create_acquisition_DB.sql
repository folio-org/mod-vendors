/*
 * Language
 */
create table if not exists Language (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/*
 * Currency
 */
create table if not exists Currency (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/*
 * VendorStatus
 */
create table if not exists Vendor_Status (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/*
 * InterfaceStatus
 */
create table if not exists Interface_Status (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/*
 * Delivery Method
 */
create table if not exists Delivery_Method (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/*
 * Statistics Format
 */
create table if not exists Statistics_Format (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/**
 * Statistics
 */
create table if not exists Statistics (
    id serial primary key,
    available boolean,
    locally_stored varchar(50),
    online_location varchar(255),
    note text,
    delivery_method_id integer references Delivery_Method,
    format_id integer references Statistics_Format
);

/**
 * Admin Info
 */
create table if not exists Admin_Info (
    id serial primary key,
    documentation text,
    integration_note varchar(255),
    hardware_req_internal varchar(255),
    hardware_req_public varchar(255),
    maintenance_win varchar(255),
    software_req_public varchar(255),
    software_req_internal varchar(255),
    uri varchar(255)
);

create table if not exists Interface_Access (
    id serial primary key,
    auth_method varchar(255),
    instructions text,
    uri varchar(255),
    ip_send_method varchar(255),
    user_id varchar(255)
);

/**
 * Contact Information
 */
create table if not exists Contact_Info (
    id serial primary key
);

/*
 * Interface
 */
create table if not exists Interface (
    id serial primary key,
    "name" varchar(255) not null,
    description varchar(255) not null,
    access_info_id integer references Interface_Access,
    admin_info_id integer references Admin_Info,
    interface_contact_id integer references Contact_Info,
    interface_status_id integer references Interface_Status,
    statistics_id integer references Statistics
);

create table if not exists Interface_Note (
    id serial primary key,
    note_text text,
    interface_id integer references Interface
);

/*
 * EDI Type
 * 
 * Models the supported EDI protocols
 */
create table if not exists Edi_Type (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);


/*
 * FTP Mode
 */
create table if not exists Ftp_Mode (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/*
 * EDI Naming Convention
 */
create table if not exists Edi_Naming_Convention (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/*
 * Send Command
 */
create table if not exists Send_Command (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/*
 * Vendor EDI Format
 */
create table if not exists Edi_Format (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/*
 * EDI Info
 */
create table if not exists Edi_Info (
    id serial primary key,
    additional_order_no boolean default false,
    allow_navigation boolean default false,
    code varchar(25),
    invoices boolean default false,
    po_lines boolean default false,
    include_fund_code boolean default false,
    do_not_prorate boolean default false,
    ftp_description varchar(255),
    ftp_username varchar(255),
    input_path varchar(512),
    output_path varchar(512),
    passive_mode boolean default false,
    port_no integer,
    secured_ftp boolean default false,
    server_name varchar(255),
    tenant_specific_code varchar(255),
    edi_type_id integer references Edi_Type,
    ftp_mode_id integer references Ftp_Mode,
    edi_naming_convention_id integer references Edi_Naming_Convention,
    send_command_id integer references Send_Command,
    edi_format_id integer references Edi_Format
);

/*
 * LibVenAcctStatus
 */
create table if not exists Lib_Ven_Acct_Status (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/*
 * Library
 */
create table if not exists Library (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/*
 * Payment Method
 */
create table if not exists Payment_Method (
    id serial primary key,
    code varchar(25) not null,
    description varchar(255) not null
);

/**
 * Vendor
 */
create table if not exists Vendor (
    id serial primary key,
    access_provider boolean default false,
    claiming_interval integer,
    code varchar(50),
    discount_percent decimal,
    expected_activation_interval integer,
    expected_invoice_interval integer,
    financial_sys_code varchar(255),
    governmental boolean default false,
    liable_for_vat boolean default false,
    licensor boolean default false,
    material_supplier boolean default false,
    "name" varchar(255) not null,
    national_tax_id varchar(50),
    renewal_activation_interval varchar(50),
    subscription_interval varchar(50),
    tax_percentage decimal,
    contact_info_id integer references Contact_Info,
    currency_id integer references Currency,
    edi_info_id integer references Edi_Info,
    interface_id integer references Interface,
    language_id integer references Language,
    vendor_status_id integer references Vendor_Status
);

/*
 * Library Vendor Account
 */
create table if not exists Library_Vendor_Account (
    id serial primary key,
    account_no varchar(25),
    discount_percent decimal,
    description varchar(255),
    note text,
    contact_info_id integer references Contact_Info,
    edi_info_id integer references Edi_Info,
    library_id integer references Library,
    payment_method_id integer references Payment_Method,
    status_id integer references Lib_Ven_Acct_Status,
    vendor_id integer references Vendor
);
