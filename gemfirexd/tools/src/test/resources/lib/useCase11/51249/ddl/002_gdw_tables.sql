------------ RELEASE NOTES -------------------
--   13 dec 2014 release ----

--added below columns into PRODUCT_CUR table
--ACTV_FLG 	VARCHAR(15)     

--added below columns into RESOURCE ROLE REF table
--DSTRCT_GRP_ASSMT_ID          BIGINT    

-- SALES_CREDIT SO_NUMBER CHANGED to 40 chars  
----------------------------------------------


DROP TABLE IF EXISTS RTRADM.SALES_GEOGRAPHY_REF;
DROP TABLE IF EXISTS RTRADM.CUSTOMER;
DROP TABLE IF EXISTS RTRADM.CUST_SITE_LOCATION;
DROP TABLE IF EXISTS RTRADM.RESOURCE_ROLE_REF;
DROP TABLE IF EXISTS RTRADM.PRODUCT_CUR;
DROP TABLE IF EXISTS RTRADM.CUST_SITE_PARTNER_EXT;
DROP TABLE IF EXISTS RTRADM.TERRITORY_ASSIGNMENT;
DROP TABLE IF EXISTS RTRADM.PRODUCT_CUR_PH_ESB_DATA;
DROP TABLE IF EXISTS RTRADM.TIME_ID; 
DROP TABLE IF EXISTS RTRADM.LOOKUP_OBIEE_DIM;
DROP TABLE IF EXISTS RTRADM.LOOKUP_D;
DROP TABLE IF EXISTS RTRADM.SALES_ORDER_TYPE_CUR;
DROP TABLE IF EXISTS RTRADM.LOOKUP_SUPERFAMILY;
DROP TABLE IF EXISTS RTRADM.CUSTOMER_DENORM;
DROP TABLE IF EXISTS RTRADM.PRODUCT_HIER;
DROP TABLE IF EXISTS RTRADM.EXCHANGE_RATE_DETAILS;
DROP TABLE IF EXISTS RTRADM.SALES_CREDITS;


CREATE TABLE RTRADM.SALES_GEOGRAPHY_REF (
ROW_WID                  		  BIGINT   NOT NULL ,
DSTRCT_ID                         BIGINT    ,
DSTRCT_TERR_ID                    VARCHAR(15)  ,
FIN_BOOK_BILL_GRP_NM              VARCHAR(250) ,
FIN_BOOK_BILL_NM                  VARCHAR(250) ,
CNTRY_NM                          VARCHAR(100) ,
DSTRCT_TERR_NM                    VARCHAR(250) ,
QUOTA_FLG                         VARCHAR(5)   ,
SLS_RPTG_LVL_NM                   VARCHAR(250) ,
AREA_TERR_ID                      VARCHAR(15)  ,
AREA_MGR_NM                       VARCHAR(100) ,
AREA_MGR_RSRC_ID                  BIGINT       ,
AREA_TERR_NM                      VARCHAR(250) ,
DSTRCT_MGR_RSRC_ID                BIGINT       ,
DSTRCT_MGR_NM                     VARCHAR(100) ,
SLS_RGN_NM                        VARCHAR(250) ,
SLS_RGN_MGR_NM                    VARCHAR(100) ,
SLS_DIV_NM                        VARCHAR(250) ,
SLS_DIV_MGR_NM                    VARCHAR(100) ,
SLS_BRM_AREA_MGR_NM               VARCHAR(100) ,
SLS_BRM_AREA_NM                   VARCHAR(250) ,
SLS_SUPR_AREA_NM                  VARCHAR(250) ,
SLS_SUPR_AREA_MGR_NM              VARCHAR(100) ,
THTR_OPS                          VARCHAR(20)  ,
THTR_SORT_ORD                     BIGINT       ,
SLS_SUPR_BRM_AREA_NM              VARCHAR(250) ,
SLS_SUPR_RGN_MGR_NM               VARCHAR(100) ,
SLS_SUPR_RGN_NM                   VARCHAR(250) ,
W_CURRENT_FLG                     VARCHAR(5)   ,
HIER_STRT_DT                      DATE,
HIER_OPEN_END_DT                  DATE
)PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;


CREATE TABLE RTRADM.CUSTOMER( 
ROW_WID                        		    BIGINT     NOT NULL,        
CUST_NM                                 VARCHAR(360), 
CUST_NUM                                VARCHAR(30) ,
CUST_ID							        BIGINT		NOT NULL,
DMSTC_ULT_ID                            BIGINT      ,
GBL_ULT_ID                              BIGINT      ,
GBL_ULT_IDNTFR                          VARCHAR(30) ,
GBL_ULT_NM                              VARCHAR(360),
DMSTC_ULT_IDNTFR                        VARCHAR(30) ,
DMSTC_ULT_NM                            VARCHAR(360),
CUST_NUM_SAP                            VARCHAR(10) 
) EVICTION BY LRUMEMSIZE 1024 EVICTACTION OVERFLOW PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS; 

 

CREATE TABLE RTRADM.CUST_SITE_LOCATION(
ROW_WID           		   BIGINT NOT NULL,        
CUST_ID           		   BIGINT  ,      
LOCTN_CNTRY_CD             VARCHAR(60)  ,
LOCTN_CNTRY_NM             VARCHAR(80)  ,
PTY_SITE_STAT              VARCHAR(1)
) EVICTION BY LRUMEMSIZE 1024 EVICTACTION OVERFLOW PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;


CREATE TABLE RTRADM.RESOURCE_ROLE_REF (
ROW_WID             		 BIGINT NOT NULL,        
RSRC_ID                      BIGINT         ,
W_CURRENT_FLG                VARCHAR(5)     ,
RSRC_SLS_FRCE                VARCHAR(150)   ,
PERS_FULL_NM                 VARCHAR(240)   ,
EMPL_USR_NM              	 VARCHAR(15)    ,
EMPL_BDGE_NUM                VARCHAR(150)   ,
DSTRCT_GRP_ASSMT_ID          BIGINT
)PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;


CREATE TABLE RTRADM.PRODUCT_CUR( 
ROW_WID                                 BIGINT NOT NULL,        
ITEM_11I_ID                             BIGINT       ,             
FMLY                                    VARCHAR(60)  ,
FORECAST_CATEGORY                       VARCHAR(40)  ,
PROFIT_CTR_CD                           VARCHAR(30)  ,
PROD_LN 					    		VARCHAR(60)  ,
RPTG_PROD_TYPE                          VARCHAR(180) ,
FORECAST_CATEGORY_GROUP                 VARCHAR(40)  ,
ITEM_NUM 								VARCHAR(40)  ,
PROD_HIER                               VARCHAR(54)  ,
ACTV_FLG 	                        VARCHAR(15)
)PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;

CREATE TABLE RTRADM.CUST_SITE_PARTNER_EXT( 
ROW_WID                      		  BIGINT  NOT NULL,      
CUST_NUM                              VARCHAR(20)  ,
CHNL_REV_PRTNR_FLG                    CHAR(1)       ,
PRTNR_FLG                             CHAR(1)       ,
PRTNR_GRPG                            VARCHAR(80)  ,
PRTNR_MSTR_GRPG                       VARCHAR(80)  ,
CUST_ID 							  BIGINT,
ACCT_ID                               VARCHAR(18),
ACCT_NM                               VARCHAR(255),
CUST_CHNL_TYPE 						  VARCHAR(20)
) EVICTION BY LRUMEMSIZE 1024 EVICTACTION OVERFLOW PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;

CREATE TABLE RTRADM.TERRITORY_ASSIGNMENT(
ROW_WID 							BIGINT NOT NULL,
DSTRCT_GP_ID 						BIGINT,
DSTRCT_TERR_ID 						VARCHAR(15),
GEO_CD 								VARCHAR(15),
GEO_CD_LVL 							VARCHAR(15),
USR_NM 								VARCHAR(25),
BDGE_NUM 							VARCHAR(10),
SEQ_NUM                             BIGINT
)PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;


CREATE TABLE RTRADM.PRODUCT_CUR_PH_ESB_DATA(
PRFT_CTR_HIER_LVL_04_NM              VARCHAR(60),
PROD_LN                              VARCHAR(60) NOT NULL 
)  	PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;


CREATE TABLE RTRADM.TIME_ID(
ROW_WID                              BIGINT NOT NULL,
CALENDAR_DATE                        DATE,
CAL_HALF                             INTEGER,
CAL_MONTH                            INTEGER,
CAL_QTR                              INTEGER,
CAL_WEEK                             INTEGER,
CAL_YEAR                             INTEGER,
PER_NAME_QTR                         VARCHAR(30),
PER_NAME_YEAR                        VARCHAR(30),
WK_OF_QTR                            INTEGER,
YR_QTR_WK_ABBR                       VARCHAR(30)
) PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;

CREATE TABLE RTRADM.LOOKUP_D(
ROW_WID								 BIGINT NOT NULL,	
ESG_USECASE10_FLAG                         VARCHAR(50),
LKUP_VAL                             VARCHAR(160),
LKUP_CD 							 VARCHAR(30),
LKUP_ID                              BIGINT,
LKUP_TYPE                            VARCHAR(50),
SRC_STRT_DT                          DATE,
SRC_END_DT                           DATE,
W_UPDATE_DT                          DATE 
) PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;

CREATE TABLE RTRADM.LOOKUP_OBIEE_DIM(
LOOKUP_TYPE                          VARCHAR(30),
CODE                                 VARCHAR(30),
DESCRIPTION                          VARCHAR(240),
VARIABLE1                            VARCHAR(20),
VARIABLE2                            VARCHAR(240)) 
PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;

CREATE TABLE RTRADM.SALES_ORDER_TYPE_CUR(
ROW_WID								 BIGINT,
SLS_ORD_CAT_CD						 VARCHAR(30),
SLS_ORD_NM							 VARCHAR(30),
SLS_ORD_DESC						 VARCHAR(240),
BOOK_BILL_ORD_TYPE	                 VARCHAR(50),
ORD_TYPE_ID	                         BIGINT,
RSN_CD	                             VARCHAR(9),
RSN_DESC	                         VARCHAR(240)) 
PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;

CREATE TABLE RTRADM.LOOKUP_SUPERFAMILY(
FMLY 								VARCHAR(60),
SUPR_FMLY 							VARCHAR(60))
PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;

INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('ATMOS','ASD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('CENTERA','ASD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('DATA CENTER MGMT','ASD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('SOFTWARE DEFINED STORAGE','ASD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('STORAGE MANAGEMENT','ASD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('STORAGE SW','ASD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('AVAMAR','DPAD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('DATA PROTECTION ADVISOR','DPAD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('DATADOMAIN','DPAD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('DLM BUSTECH','DPAD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('EDL','DPAD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('NETWORKER','DPAD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('RECOVERPOINT FAMILY','DPAD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('SOURCEONE','DPAD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('STORAGE VIRTUALIZATION','DPAD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('DATA PROTECTION SUITE','DPAD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('CELERRA','EMSD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('CLARIION','EMSD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('SYMMETRIX','EMSD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('UNIFIED','EMSD');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('XTREMIO','FLASH');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('XTREMSERVERFLASH','FLASH');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('PIVOTAL RESELL','PIVOTAL');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('PIVOTAL LABS FAMILY','PIVOTAL');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('COMMON FAMILY','STG OTHER');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('EDM','STG OTHER');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('RSA-IDP Other','SECURITY');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('RSA-IDP SID','SECURITY');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('RSA-OTHER','SECURITY');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('RSA-SMC','SECURITY');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('BROKERAGE','THIRD PARTY');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('CONNECTRIX','THIRD PARTY');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('USECASE11 SELECT','THIRD PARTY');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('CONTENT AND CASE MGMT','IIG');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('ONDEMAND','IIG');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('EMCC','EMCC');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('GREENPLUM','GREENPLUM');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('DCA','DCA');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('GLOBAL SERVICES','GLOBAL SERVICES');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('ISILON','ISILON');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('VMWARE FAMILY','VMWARE');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('MOZY','MOZY');
INSERT INTO RTRADM.LOOKUP_SUPERFAMILY VALUES ('OTHER','OTHER');


CREATE TABLE RTRADM.CUSTOMER_DENORM
(
  ROW_WID                   BIGINT,
  CUSTOMER_HASH_ID          VARCHAR(32),
  BT_CUST_ID                BIGINT,
  EU_CUST_ID                BIGINT,
  IA_CUST_ID                BIGINT,
  ST_CUST_ID                BIGINT,
  SO_CUST_ID                BIGINT,
  TIER2_CUST_ID             BIGINT,
  BT_LOCTN_CNTRY_CD         VARCHAR(60),
  BT_LOCTN_CNTRY_NM         VARCHAR(80),
  BT_CUST_NM                VARCHAR(240),
  BT_CUST_NUM               VARCHAR(30),
  BT_GBL_ULT_ID             BIGINT,
  BT_GBL_ULT_IDNTFR         VARCHAR(30),
  BT_GBL_ULT_NM             VARCHAR(360),
  BT_DMSTC_ULT_ID           BIGINT,
  BT_DMSTC_ULT_IDNTFR       VARCHAR(30),
  BT_DMSTC_ULT_NM           VARCHAR(360),
  EU_LOCTN_CNTRY_CD         VARCHAR(60),
  EU_LOCTN_CNTRY_NM         VARCHAR(80),
  EU_CUST_NM                VARCHAR(240),
  EU_CUST_NUM               VARCHAR(30),
  EU_GBL_ULT_ID             BIGINT,
  EU_GBL_ULT_IDNTFR         VARCHAR(30),
  EU_GBL_ULT_NM             VARCHAR(360),
  EU_DMSTC_ULT_ID           BIGINT,
  EU_DMSTC_ULT_IDNTFR       VARCHAR(30),
  EU_DMSTC_ULT_NM           VARCHAR(360),
  IA_LOCTN_CNTRY_CD         VARCHAR(60),
  IA_LOCTN_CNTRY_NM         VARCHAR(80),
  IA_CUST_NM                VARCHAR(240),
  IA_CUST_NUM               VARCHAR(30),
  IA_GBL_ULT_ID             BIGINT,
  IA_GBL_ULT_IDNTFR         VARCHAR(30),
  IA_GBL_ULT_NM             VARCHAR(360),
  IA_DMSTC_ULT_ID           BIGINT,
  IA_DMSTC_ULT_IDNTFR       VARCHAR(30),
  IA_DMSTC_ULT_NM           VARCHAR(360),
  ST_LOCTN_CNTRY_CD         VARCHAR(60),
  ST_LOCTN_CNTRY_NM         VARCHAR(80),
  ST_CUST_NM                VARCHAR(240),
  ST_CUST_NUM               VARCHAR(30),
  ST_GBL_ULT_ID             BIGINT,
  ST_GBL_ULT_IDNTFR         VARCHAR(30),
  ST_GBL_ULT_NM             VARCHAR(360),
  ST_DMSTC_ULT_ID           BIGINT,
  ST_DMSTC_ULT_IDNTFR       VARCHAR(30),
  ST_DMSTC_ULT_NM           VARCHAR(360),
  SO_LOCTN_CNTRY_CD         VARCHAR(60),
  SO_LOCTN_CNTRY_NM         VARCHAR(80),
  SO_CUST_NM                VARCHAR(240),
  SO_CUST_NUM               VARCHAR(30),
  SO_GBL_ULT_ID             BIGINT,
  SO_GBL_ULT_IDNTFR         VARCHAR(30),
  SO_GBL_ULT_NM             VARCHAR(360),
  SO_DMSTC_ULT_ID           BIGINT,
  SO_DMSTC_ULT_IDNTFR       VARCHAR(30),
  SO_DMSTC_ULT_NM           VARCHAR(360),
  PRTNR_CHNL_REV_PRTNR_FLG  CHAR(1),
  PRTNR_FLG                 CHAR(1),
  PRTNR_GRPG                VARCHAR(80),
  PRTNR_MSTR_GRPG           VARCHAR(80),
  TIER2_CHNL_REV_PRTNR_FLG  CHAR(1),
  TIER2_PRTNR_FLG           CHAR(1),
  TIER2_PRTNR_GRPG          VARCHAR(80),
  TIER2_PRTNR_MSTR_GRPG     VARCHAR(80),
  TIER2_CUST_NM             VARCHAR(240),
  TIER2_CUST_NUM            VARCHAR(30),
  W_INSERT_DT               TIMESTAMP,
  W_LAST_UPDATED_BY         VARCHAR(25),
  W_ROW_HASH                VARCHAR(32),
  W_SESSION_NUMBER          BIGINT,
  W_SOURCE_SYSTEM           VARCHAR(40),
  W_UPDATE_DT               TIMESTAMP
)EVICTION BY LRUMEMSIZE 1024 EVICTACTION OVERFLOW PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;


CREATE TABLE RTRADM.PRODUCT_HIER(
ROW_WID                     BIGINT,
PROD_HIER                   VARCHAR(54),
PROD_LN                     VARCHAR(60),
VRSN                        VARCHAR(60),
FMLY                        VARCHAR(60),
ROW_HASH                    VARCHAR(32),
CORP_FMLY                   VARCHAR(60),
PROCESS                     DECIMAL,
INSERT_DT                   DATE,
UPDATE_DT                   DATE
)PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;

CREATE TABLE RTRADM.EXCHANGE_RATE_DETAILS(
ROW_WID                    BIGINT,
Exchg_Rt                   VARCHAR(30),
Exchg_Rt_Dt                DATE,
Exchg_Rt_Type              VARCHAR(20),
From_Currncy               VARCHAR(20),
To_Currncy				   VARCHAR(20),
Last_Updated_By          VARCHAR(20),
Insert_Dt				   DATE,
Update_Dt				   DATE,
Dly_Rt					   VARCHAR(30),
Mth_End_Rt				   VARCHAR(30),
cmmsn_rt				   VARCHAR(30),
rev_prc_rt				   VARCHAR(30)	
)PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;

CREATE TABLE RTRADM.SALES_CREDITS 
(
  SALES_CREDIT_ID BIGINT, 
  SALES_CREDIT_TYPE_ID VARCHAR(2), 
  SALESREP_ID BIGINT, 
  SO_NUMBER VARCHAR(40), 
  DISTRICT_ID BIGINT, 
  PERCENT DOUBLE, 
  LAST_UPDATE_DATE DATE, 
  EFFECTIVE_END_DATE DATE, 
  SALESREP_NUMBER VARCHAR(100), 
  GDW_PROCESS_FLAG CHAR(1) DEFAULT 'N', 
  SOURCE_BUCKET VARCHAR(20), 
  AIC_LAST_UPDATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
  THEATER VARCHAR(50)
) EVICTION BY LRUMEMSIZE 1024 EVICTACTION OVERFLOW PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;

-- CREATE TABLE RTRADM.SALES_CREDITS
-- (
-- SALES_CREDIT_ID 			BIGINT,
-- SALES_CREDIT_TYPE_ID 		VARCHAR(2),
-- SO_NUMBER 					VARCHAR(40),
-- SALESREP_ID 				BIGINT,
-- SALESREP_NUMBER 			VARCHAR(100),
-- DISTRICT_ID 			    BIGINT,
-- PERCENT 				    DOUBLE,
-- LAST_UPDATE_DATE 			DATE,
-- EFFECTIVE_END_DATE 			DATE,
-- SOURCE_BUCKET 				VARCHAR(20),
-- GDW_PROCESS_FLAG            CHAR(1) DEFAULT 'N',
-- AIC_LAST_UPDATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
-- THEATER                     VARCHAR(50)
-- ) EVICTION BY LRUMEMSIZE 1024 EVICTACTION OVERFLOW PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS;


grant select on RTRADM.CUSTOMER to rtr_bi;
grant select on RTRADM.CUST_SITE_LOCATION to rtr_bi;
grant select on RTRADM.CUST_SITE_PARTNER_EXT to rtr_bi;
grant select on RTRADM.LOOKUP_D to rtr_bi;
grant select on RTRADM.LOOKUP_OBIEE_DIM to rtr_bi;
grant select on RTRADM.PRODUCT_CUR to rtr_bi;
grant select on RTRADM.PRODUCT_CUR_PH_ESB_DATA to rtr_bi;
grant select on RTRADM.RESOURCE_ROLE_REF to rtr_bi;
grant select on RTRADM.RTR_REPORT_DATA to rtr_bi;
grant select on RTRADM.SALES_GEOGRAPHY_REF to rtr_bi;
grant select on RTRADM.TERRITORY_ASSIGNMENT to rtr_bi;
grant select on RTRADM.TIME_ID to rtr_bi;
grant select on RTRADM.SALES_ORDER_TYPE_CUR to rtr_bi;
grant select on RTRADM.LOOKUP_SUPERFAMILY to rtr_bi;
grant select on RTRADM.CUSTOMER_DENORM to rtr_bi;
grant select on RTRADM.PRODUCT_HIER to rtr_bi;
grant select on RTRADM.EXCHANGE_RATE_DETAILS to rtr_bi;
grant SELECT ON RTRADM.SALES_CREDITS TO RTR_BI;
