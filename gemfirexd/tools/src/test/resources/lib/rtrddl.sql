CREATE DISKSTORE RTRADM_DATA01;
CREATE TABLE RTRADM.RTR_REPORT_DATA(
  SOURCE_BUCKET                                         VARCHAR(40)             NOT NULL,
  CREATION_DATE                                         DATE            ,
  BT_CUST_WID                                                   BIGINT                  ,
  IA_CUST_WID                                                   BIGINT                  ,
  EU_CUST_WID                                                   BIGINT                  ,
  ST_CUST_WID                                                   BIGINT                  ,
  SO_CUST_WID                                                   BIGINT                  ,
  RPTG_DT_WID                                                   BIGINT                  ,
  QTE_NUM                                                       VARCHAR(40)             ,
  REP_PERC                                                      BIGINT                  ,
  LIST_PRICE                                                    DOUBLE                  ,
  PROD_CUR_WID                                          BIGINT                  ,
  REVENUE                                                               DOUBLE                  ,
  ORDRD_DT                                                      DATE            ,
  ORD_NUM                                                       VARCHAR(40)             ,
  ORD_TYP                                                       VARCHAR(30)             ,
  SLS_CHNL_CD                                                   VARCHAR(30)             ,
  BUCKET                                                        VARCHAR(30)             ,
  QTY                                                                   BIGINT          ,
  OPP_NO                                                        VARCHAR(150)    ,
  DEAL_NUM                                                      VARCHAR(150)    ,
  TRX_TYPE                                                      VARCHAR(50)             ,
  TRX_NUMBER                                                    VARCHAR(50)             ,
  DAY32                                                 VARCHAR(150)    ,
  LST_UPD_DT                                                    DATE            ,
  FRCST_CLS_DT                                          DATE            ,
  ORD_SRC                                                       VARCHAR(30)             ,
  FORECAST_CAT                                          VARCHAR(40)             ,
  FORECAST_CAT_GRP                                      VARCHAR(40)             ,
  FMLY                                                  VARCHAR(60)             ,
  SUPR_FMLY                           VARCHAR(60)     ,
  RPTG_PRD_TYPE                                         VARCHAR(180)    ,
  RPTG_YR_QTR_ABBRV                             VARCHAR(150)    ,
  ESG_EMC_FLAG                                          VARCHAR(50)             ,
  TIER2_CUST_WID                                                BIGINT                  ,
  INT_WRNTY                                                     DECIMAL                 ,
  PST_WRNTY                                                     DECIMAL                 ,
  WRNTY_CONC                                                    DECIMAL                 ,
  SLS_REP_ID                                                    BIGINT                  ,
  DSTRCT_ID                                                     BIGINT                  ,
  REV_FLG                                                       CHAR(2)         ,
  ACQCO_FLG                                                     CHAR(2)         ,
  MGR_FORECAST_AMT                                      BIGINT          ,
  MAINT_TYPE                                                    VARCHAR(40)             ,
  PPM_3YEAR_REV                                         BIGINT          ,
  GOAL_DT                                                       DATE            ,
  GOAL_CAT                                                      VARCHAR(50)             ,
  GOAL_REF_NUM                                          BIGINT                  ,
  GOAL_CMNT                                                     VARCHAR(200)    ,
  GOAL_INV_AMT                                          DECIMAL                 ,
  GOAL_AMT                                                      DECIMAL                 ,
  DEL_FLG                                                       CHAR(1)                 ,
  W_EFFECTIVE_FROM_DT                                   DATE                    ,
  W_EFFECTIVE_TO_DT                                     DATE                    ,
  W_CURRENT_FLG                                         VARCHAR(5)              ,
  MGRFORECAST_1                                         VARCHAR(50)             ,
  MGRFORECAST_2                                         VARCHAR(50)             ,
  MGRFORECAST_3                                         VARCHAR(50)             ,
  MGRFORECAST_4                                         VARCHAR(50)             ,
  MGRFORECAST_5                                         VARCHAR(50)             ,
  MGRFORECAST_6                                         VARCHAR(50)             ,
  MGRFORECAST_7                                         VARCHAR(50)             ,
  MGRFORECAST_8                                         VARCHAR(50)             ,
  MGRFORECAST_9                                         VARCHAR(50)             ,
  MGRFORECAST_10                                                VARCHAR(50)             ,
  QUOTELINKEDTOOPP                                      VARCHAR(50)             ,
  STATUS                                                        VARCHAR(50)         ,
  QUOTE_HEADER_ID                                               VARCHAR(50)     ,
  ORDER_CREATION_TIME                                   TIMESTAMP       ,
  BOOKBILLORDERTYPE                   VARCHAR(40)     ,
  PONUMBER                            VARCHAR(40)     ,
  SALES_ORDER_LINE_NUMBER             VARCHAR(40)     ,
  CUSTOMER_HASH_ID                    VARCHAR(32)     ,
  RECORD_STATUS                                         VARCHAR(40)     ,
  PRODUCT_HIERARCHY                   VARCHAR(54)     ,
  AIC_LAST_UPDATE                     DATE            ,
  ORDER_REASON                        VARCHAR(50)
  ) PERSISTENT 'RTRADM_DATA01' ASYNCHRONOUS
