SET SCHEMA TPCHGFXD;

--CREATE INDEX I_P_SIZE ON PART (P_SIZE);
--CREATE INDEX I_P_TYPE ON PART (P_TYPE);
--CREATE INDEX I_L_ORDERKEY ON LINEITEM (L_ORDERKEY);
--CREATE INDEX I_L_RETURNFLAG ON LINEITEM (L_RETURNFLAG);
CREATE INDEX I_O_ORDERDATE ON ORDERS (O_ORDERDATE);
