/**
 * Initialization
 */
set client_encoding to 'UTF8';
set standard_conforming_strings to on;
set check_function_bodies to false;
set client_min_messages to warning;

/**
 * Import third-party modules that will allow for UUID generation, if it is not installed yet.
 */
create extension if not exists "uuid-ossp";

/**
 * The following tables all relate to Contacts. These may need to be decoupled from the Vendors module
 * and encapsulated into its own Contacts module.
 */

create table "phone_number" (
  "id" uuid default uuid_generate_v4() primary key,
  "country_code" varchar(25),
  "area_code" varchar(25),
  "phone_number" varchar(25) not null
);

create table "email" (
  "id" uuid default uuid_generate_v4() primary key,
  "value" varchar(255) not null
);

create table "address" (
  "id" uuid default uuid_generate_v4() primary key,
  "address_line_1" varchar(255),
  "address_line_2" varchar(255),
  "city" varchar(100),
  "region" varchar(100),
  "postal_code" varchar(100),
  "country" varchar(100)
);

create table "person" (
  "id" uuid default uuid_generate_v4() primary key,
  "prefix" varchar(100),
  "first_name" varchar(255) not null,
  "last_name" varchar(255),
  "language" varchar(255),
  "notes" text,
  "phone_number_id" uuid references "phone_number",
  "email_id" uuid references "email",
  "address_id" uuid references "address"
);



/**
 * The following tables all relate to the Vendor-module
 */

create table "category" (
  "id" uuid default uuid_generate_v4() primary key,
  "value" varchar(100) not null
);

insert into "category" ("value") values
('Returns'), ('Payments'), ('Customer Service'), ('Shipments');

create table "contact_category" (
  "id" uuid default uuid_generate_v4() primary key,
  "value" varchar(100) not null
);

insert into "contact_category" ("value") values
('Accounting'), ('Books'), ('Customer Service'), ('Databases'), ('Ebooks'), ('Econtent'),
('General'), ('Journals'), ('Licenses'), ('Primary'), ('Sales'), ('Serials');

create table "edi_type" (
  "id" uuid default uuid_generate_v4() primary key,
  "code" varchar(25) not null,
  "description" varchar(100)
);

insert into "edi_type" ("code") values
('014/EAN'), ('31B/US-SAN'), ('091/Vendor-assigned'), ('092/Customer-assigned');

create table "payment_method" (
  "id" uuid default uuid_generate_v4() primary key,
  "code" varchar(25) not null,
  "description" varchar(100)
);

insert into "payment_method" ("code") values
('EFT'), ('Bank Draft'), ('Paper Check'), ('Credit Card/P-Card'), ('Deposit Account'), ('Cash');

create table "statistics_format" (
  "id" uuid default uuid_generate_v4() primary key,
  "code" varchar(25) not null,
  "description" varchar(100)
);

insert into "statistics_format" ("code") values
('Delimited'), ('Excel'), ('CSV'), ('PDF'), ('ASCII'), ('HTML'), ('Other');

create table "delivery_method" (
  "id" uuid default uuid_generate_v4() primary key,
  "code" varchar(25) not null,
  "description" varchar(100)
);

insert into "delivery_method" ("code") values
('Online'), ('FTP'), ('Email');

create table "ftp_format" (
  "id" uuid default uuid_generate_v4() primary key,
  "code" varchar(25) not null,
  "description" varchar(100)
);

insert into "ftp_format" ("code") values
('SFTP'), ('FTP');

create table "ftp_mode" (
  "id" uuid default uuid_generate_v4() primary key,
  "code" varchar(25) not null,
  "description" varchar(100)
);

insert into "ftp_mode" ("code") values
('ASCII'), ('BINARY');

create table "vendor_status" (
  "id" uuid default uuid_generate_v4() primary key,
  "code" varchar(25) not null,
  "description" varchar(100)
);

insert into "vendor_status" ("code") values
('Active'), ('Pending'), ('Inactive');

create table "lib_ven_acct_status" (
  "id" uuid default uuid_generate_v4() primary key,
  "code" varchar(25) not null,
  "description" varchar(100)
);

insert into "lib_ven_acct_status" ("code") values
('Active'), ('Pending'), ('Inactive');


create table "edi_info" (
  "id" uuid default uuid_generate_v4() primary key,
  "vendor_edi_code" varchar(100),
  "vendor_edi_type" varchar(100),           -- This pulls values from the "code" field of "edi_type"
  "lib_edi_code" varchar(100),
  "lib_edi_type" varchar(100),              -- This pulls values from the "code" field of "edi_type"
  "edi_naming_convention" varchar(255),
  "prorate_tax" boolean,
  "prorate_fees" boolean,
  "send_acct_num" boolean,
  "support_order" boolean,
  "support_invoice" boolean,
  "notes" text,
  "ftp_format" varchar(255),                -- i.e. SFTP, FTP
  "ftp_mode" varchar(255),                  -- i.e. ASCII, BINARY
  "ftp_conn_mode" varchar(255),             -- i.e. ACTIVE, PASSIVE
  "ftp_port" varchar(10),                   -- Defaults: 21 (FTP)/22 (SFTP)
  "server_address" varchar(2000),
  "username" varchar(255),
  "password" varchar(255),
  "order_directory" varchar(255),
  "invoice_directory" varchar(255),
  "send_to_emails" text,                    -- comma-separated string
  "notify_all_edi" bool,
  "notify_invoice_only" bool,
  "notify_error_only" bool
);

create table "vendor" (
  "id" uuid default uuid_generate_v4() primary key,
  "name" varchar(255) not null,
  "code" varchar(50),
  "description" text,
  "vendor_status" varchar(50),                   -- Active, Pending, Inactive
  "language" varchar(255),
  "erp_code" varchar(50),
  "payment_method" varchar(100),                 -- e.g. EFT, Bank Draft, Paper Check, Credit Card/P-Card, Deposit Account, Cash
  "access_provider" bool,
  "governmental" bool,
  "licensor" bool,
  "material_supplier" bool,
  "claiming_interval" int,
  "discount_percent" decimal,
  "expected_activation_interval" int,
  "expected_invoice_interval" int,
  "renewal_activation_interval" int,
  "subscription_interval" int,
  "liable_for_vat" bool,
  "tax_id" varchar(50),
  "tax_percentage" decimal,
  "edi_info_id" uuid references "edi_info"
);

create table "vendor_name" (
  "id" uuid default uuid_generate_v4() primary key,
  "value" varchar(255) not null,
  "description" varchar(100),
  "vendor_id" uuid references "vendor"
);

create table "vendor_currency" (
  "id" uuid default uuid_generate_v4() primary key,
  "currency" varchar(255) not null,
  "vendor_id" uuid references "vendor"
);

create table "vendor_interface" (
  "id" uuid default uuid_generate_v4() primary key,
  "name" varchar(255),
  "uri" varchar(255) not null,
  "username" varchar(100),
  "password" varchar(255),                      -- Would this need to be encrypted?
  "notes" text,
  "available" boolean,
  "delivery_method" varchar(255),               -- e.g. Online, FTP, Email
  "statistics_format" varchar(255),             -- e.g. Delimited, Excel, CSV, PDF, ASCII, HTML, Other
  "locally_stored" varchar(255),
  "online_location" varchar(255),
  "statistics_notes" text,
  "vendor_id" uuid references "vendor"
);

create table "agreement" (
  "id" uuid default uuid_generate_v4() primary key,
  "discount" decimal,
  "name" varchar(255),
  "notes" text,
  "reference_url" varchar(255) not null,
  "vendor_id" uuid references "vendor"
);

create table "library_vendor_acct" (
  "id" uuid default uuid_generate_v4() primary key,
  "name" varchar(255),
  "payment_method" varchar(100),                 -- e.g. EFT, Bank Draft, Paper Check, Credit Card/P-Card, Deposit Account, Cash
  "account_no" varchar(100) not null,
  "lib_ven_acct_status" varchar(100) not null,   -- Active, Inactive, Pending
  "description" varchar (255),
  "contact_info" varchar(255),
  "apsystem_no" varchar(100),                    -- Account Payable System No.
  "notes" text,
  "library_code" varchar(100),
  "library_edi_code" varchar(100),
  "vendor_id" uuid references "vendor"
);

create table "note" (
  "id" uuid default uuid_generate_v4() primary key,
  "description" varchar(255) not null,
  "timestamp" timestamp,
  "user_id" varchar(255),                       -- Weak reference to a user ID
  "vendor_id" uuid references "vendor"
);

create table "job" (
  "id" uuid default uuid_generate_v4() primary key,
  "is_scheduled" boolean,
  "start_date" timestamp,
  "time" time,
  "is_monday" boolean,
  "is_tuesday" boolean,
  "is_wednesday" boolean,
  "is_thursday" boolean,
  "is_friday" boolean,
  "is_saturday" boolean,
  "is_sunday" boolean,
  "scheduling_notes" text,
  "vendor_id" uuid references "vendor"
);

create table "vendor_address" (
  "id" uuid default uuid_generate_v4() primary key,
  "language" varchar(255),
  "address" varchar(255),                        -- Weak reference to an "address"
  "san_code" varchar(255),
  "vendor_id" uuid references "vendor"
);

create table "vendor_address_category" (
  "vendor_address_id" uuid references "vendor_address",
  "category_id" uuid references "category"
);

create table "vendor_phone" (
  "id" uuid default uuid_generate_v4() primary key,
  "language" varchar(255),
  "phone_number" varchar(255),                   -- Weak reference to a "phone_number"
  "vendor_id" uuid references "vendor"
);

create table "vendor_phone_category" (
  "vendor_phone_id" uuid references "vendor_phone",
  "category_id" uuid references "category"
);

create table "vendor_email" (
  "id" uuid default uuid_generate_v4() primary key,
  "language" varchar(255),
  "email" varchar(255),                          -- Weak reference to an "email"
  "vendor_id" uuid references "vendor"
);

create table "vendor_email_category" (
  "vendor_email_id" uuid references "vendor_email",
  "category_id" uuid references "category"
);

create table "vendor_contact" (
  "id" uuid default uuid_generate_v4() primary key,
  "language" varchar(255),
  "contact_person_id" varchar(255),             -- Weak reference to a "contact_person_id"
  "vendor_id" uuid references "vendor"
);

create table "vendor_contact_category" (
  "vendor_contact_id" uuid references "vendor_contact",
  "category_id" uuid references "contact_category"
);

create table "vendor_url" (
  "id" uuid default uuid_generate_v4() primary key,
  "value" varchar(2000) not null,
  "language" varchar(255),
  "notes" text,
  "vendor_id" uuid references "vendor"
);

create table "vendor_url_category" (
  "vendor_url_id" uuid references "vendor_url",
  "category_id" uuid references "category"
);