-- connect client 'localhost:1530';
CREATE TABLE CDSDBA.XML_DOC_1 (
    XML_DOC_ID_NBR DECIMAL(19) NOT NULL,
    STRUCTURE_ID_NBR DECIMAL(22) NOT NULL,
    CREATE_MINT_CD CHAR(1) NOT NULL,
    MSG_PAYLOAD_QTY DECIMAL(22) NOT NULL,
    MSG_PAYLOAD1_IMG CLOB(2000) NOT NULL,
    MSG_PAYLOAD2_IMG CLOB(2000),
    MSG_PAYLOAD_SIZE_NBR DECIMAL(22),
    MSG_PURGE_DT DATE,
    DELETED_FLG CHAR(1) NOT NULL,
    LAST_UPDATE_SYSTEM_NM VARCHAR(30),
    LAST_UPDATE_TMSTP TIMESTAMP NOT NULL,
    MSG_MAJOR_VERSION_NBR DECIMAL(22),
    MSG_MINOR_VERSION_NBR DECIMAL(22),
    OPT_LOCK_TOKEN_NBR DECIMAL(22) DEFAULT 1,
    PRESET_DICTIONARY_ID_NBR DECIMAL(22) DEFAULT 0 NOT NULL
)
PARTITION BY COLUMN (STRUCTURE_ID_NBR)
REDUNDANCY 1
OFFHEAP;
CREATE UNIQUE INDEX CDSDBA.XML_DOC_1_UK1
    ON CDSDBA.XML_DOC_1(XML_DOC_ID_NBR,STRUCTURE_ID_NBR);
    
CREATE TABLE CDSDBA.XML_IDX_1_1 (
      IDX_COL1 VARCHAR(120),
      IDX_COL2 VARCHAR(60),
      IDX_COL3 VARCHAR(60),
      IDX_COL4 VARCHAR(60),
      IDX_COL5 VARCHAR(2),
      XML_DOC_ID_NBR DECIMAL(19) NOT NULL,
      CREATE_MINT_CD CHAR(1) NOT NULL,
      LAST_UPDATE_SYSTEM_NM VARCHAR(30),
      LAST_UPDATE_TMSTP TIMESTAMP NOT NULL
  )
  PARTITION BY COLUMN (XML_DOC_ID_NBR)  REDUNDANCY 1 OFFHEAP;
  
  CREATE TABLE CDSDBA.XML_IDX_1_2 (
      IDX_COL1 VARCHAR(2),
      IDX_COL2 VARCHAR(60),
      XML_DOC_ID_NBR DECIMAL(19) NOT NULL,
      CREATE_MINT_CD CHAR(1) NOT NULL,
      LAST_UPDATE_SYSTEM_NM VARCHAR(30),
      LAST_UPDATE_TMSTP TIMESTAMP NOT NULL
  )
  PARTITION BY COLUMN (XML_DOC_ID_NBR)
  REDUNDANCY 1 OFFHEAP;
  
  CREATE TABLE CDSDBA.XML_IDX_1_3 (
      IDX_COL1 VARCHAR(60),
      XML_DOC_ID_NBR DECIMAL(19) NOT NULL,
      CREATE_MINT_CD CHAR(1) NOT NULL,
      LAST_UPDATE_SYSTEM_NM VARCHAR(30),
      LAST_UPDATE_TMSTP TIMESTAMP NOT NULL
  )
  PARTITION BY COLUMN (XML_DOC_ID_NBR)
  REDUNDANCY 1 OFFHEAP;
  
  CREATE TABLE CDSDBA.XML_IDX_1_4 (
      IDX_COL1 DECIMAL(19),
      XML_DOC_ID_NBR DECIMAL(19) NOT NULL,
      CREATE_MINT_CD CHAR(1) NOT NULL,
      LAST_UPDATE_SYSTEM_NM VARCHAR(30),
      LAST_UPDATE_TMSTP TIMESTAMP NOT NULL
  )
  PARTITION BY COLUMN (XML_DOC_ID_NBR)
  REDUNDANCY 1 OFFHEAP;
  
  CREATE TABLE CDSDBA.XML_IDX_1_5 (
      IDX_COL1 DECIMAL(19),
      XML_DOC_ID_NBR DECIMAL(19) NOT NULL,
      CREATE_MINT_CD CHAR(1) NOT NULL,
      LAST_UPDATE_SYSTEM_NM VARCHAR(30),
      LAST_UPDATE_TMSTP TIMESTAMP NOT NULL
  )
  PARTITION BY COLUMN (XML_DOC_ID_NBR)
  REDUNDANCY 1 OFFHEAP;
  
  CREATE TABLE CDSDBA.XML_IDX_1_6 (
      IDX_COL1 DECIMAL(22),
      XML_DOC_ID_NBR DECIMAL(19) NOT NULL,
      CREATE_MINT_CD CHAR(1) NOT NULL,
      LAST_UPDATE_SYSTEM_NM VARCHAR(30),
      LAST_UPDATE_TMSTP TIMESTAMP NOT NULL
  )
  PARTITION BY COLUMN (XML_DOC_ID_NBR)
  REDUNDANCY 1 OFFHEAP;
  
  CREATE TABLE CDSDBA.XML_IDX_1_7 (
      IDX_COL1 DECIMAL(38,2),
      XML_DOC_ID_NBR DECIMAL(19) NOT NULL,
      CREATE_MINT_CD CHAR(1) NOT NULL,
      LAST_UPDATE_SYSTEM_NM VARCHAR(30),
      LAST_UPDATE_TMSTP TIMESTAMP NOT NULL
  )
  PARTITION BY COLUMN (XML_DOC_ID_NBR)
  REDUNDANCY 1 OFFHEAP;
  
  CREATE TABLE CDSDBA.XML_IDX_1_8 (
      IDX_COL1 DECIMAL(22),
      XML_DOC_ID_NBR DECIMAL(19) NOT NULL,
      CREATE_MINT_CD CHAR(1) NOT NULL,
      LAST_UPDATE_SYSTEM_NM VARCHAR(30),
      LAST_UPDATE_TMSTP TIMESTAMP NOT NULL
  )
  PARTITION BY COLUMN (XML_DOC_ID_NBR)
  REDUNDANCY 1 OFFHEAP;
  
  CREATE TABLE CDSDBA.DOMAIN_CONTROL (
      DOMAIN_ID_NBR DECIMAL(22) NOT NULL,
      DOMAIN_NM VARCHAR(60) NOT NULL,
      DOMAIN_USAGE_DESC VARCHAR(60),
      CREATE_TMSTP TIMESTAMP NOT NULL,
      LAST_UPDATE_SYSTEM_NM VARCHAR(30),
      LAST_UPDATE_TMSTP TIMESTAMP NOT NULL,
      JAVA_LIB_PATH_DESC VARCHAR(2000),
      APP_ID_NBR VARCHAR(20) DEFAULT 'NO_APP_ID' NOT NULL,
      ACTIVE_FLG CHAR(1) DEFAULT 'Y'
  )
  REPLICATE
  PERSISTENT ASYNCHRONOUS OFFHEAP;
  
  CREATE TABLE CDSDBA.APPLICATION_CONTROL (
    APP_ID_NM VARCHAR(16) NOT NULL,
    TRANS_PER_SECOND_NBR DECIMAL(16) NOT NULL,
    ENFORCE_CD CHAR NOT NULL,
    LAST_UPDATE_TMSTP TIMESTAMP NOT NULL
  )
  REPLICATE
  PERSISTENT ASYNCHRONOUS OFFHEAP;
  
  CREATE TABLE CDSDBA.DEPENDENCY_CONTROL (
      STRUCTURE_ID_NBR DECIMAL(22) NOT NULL,
      DEPENDENT_STRUCTURE_ID_NBR DECIMAL(22) NOT NULL,
      LAST_UPDATE_SYSTEM_NM VARCHAR(30),
      LAST_UPDATE_TMSTP TIMESTAMP NOT NULL
  )
  REPLICATE
  PERSISTENT ASYNCHRONOUS OFFHEAP;
  
  CREATE TABLE CDSDBA.HOST_REGISTRATION (
      HOST_ID_NBR DECIMAL(22) NOT NULL,
      HOST_NM VARCHAR(250) NOT NULL,
      DATA_CENTER_ID_NBR DECIMAL(22) NOT NULL,
      CREATE_TMSTP TIMESTAMP NOT NULL,
      LAST_REGISTRATION_MS_NBR DECIMAL(22) NOT NULL
  )
  REPLICATE
  PERSISTENT ASYNCHRONOUS OFFHEAP;
  
  CREATE TABLE CDSDBA.INDEX_CONTROL (
      STRUCTURE_ID_NBR DECIMAL(22) NOT NULL,
      INDEX_ID_NBR DECIMAL(22) NOT NULL,
      INDEX_COLUMN_ORDER_NBR DECIMAL(22) NOT NULL,
      ACTIVE_FLG CHAR(1) NOT NULL,
      XPATH_EXPR_DESC VARCHAR(2000) NOT NULL,
      LAST_UPDATE_SYSTEM_NM VARCHAR(30),
      LAST_UPDATE_TMSTP TIMESTAMP NOT NULL,
      CREATE_PRIORITY_CD CHAR(1) NOT NULL,
      INDEX_COLUMN_FORMAT_CD CHAR(2) NOT NULL,
      NORMALIZATION_CD DECIMAL(22) DEFAULT 1 NOT NULL,
      SUPPORTED_COMPR_BIT_MAP_NBR DECIMAL(22) DEFAULT 1 NOT NULL,
      DATA_TYPE_CD VARCHAR(20) DEFAULT 'STRING',
      RETURN_RESULTSCOUNT_FLG CHAR(1) DEFAULT 'N' NOT NULL,
      SUBINDEX_ID_NBR DECIMAL(22) DEFAULT 0 NOT NULL,
      SUBINDEX_COLUMN_NBR DECIMAL(22) DEFAULT 0 NOT NULL,
      SUPPORTED_FLAGS_BITMAP DECIMAL(22) DEFAULT 0 NOT NULL
  )
  REPLICATE
  PERSISTENT ASYNCHRONOUS OFFHEAP;
  

