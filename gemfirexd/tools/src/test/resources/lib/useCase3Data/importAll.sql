delete from APP.TD_INSTRUMENT_SCD;
delete from APP.TD_POSN_EXTENDED_KEY;
delete from APP.TD_TRADER_SCD;
delete from APP.TF_PL_ADJ_REPORT;
delete from APP.TF_PL_POSITION_PTD;
delete from APP.TF_PL_POSITION_YTD;
delete from APP.TL_SOURCE_SYSTEM;
delete from APP.TX_PL_USER_POSN_MAP;
call syscs_util.import_table_ex('APP', 'TD_INSTRUMENT_SCD', '<path_prefix>/lib/useCase3Data/TD_INSTRUMENT_SCD.dat', '|', NULL, NULL, 0, 0, 6, 0, 'ImportOra', NULL);
call syscs_util.import_table_ex('APP', 'TD_POSN_EXTENDED_KEY', '<path_prefix>/lib/useCase3Data/TD_POSN_EXTENDED_KEY.dat', '|', NULL, NULL, 0, 0, 6, 0, 'ImportOra', NULL);
call syscs_util.import_table_ex('APP', 'TD_TRADER_SCD', '<path_prefix>/lib/useCase3Data/TD_TRADER_SCD.dat', '|', NULL, NULL, 0, 0, 6, 0, 'ImportOra', NULL);
call syscs_util.import_table_ex('APP', 'TF_PL_ADJ_REPORT', '<path_prefix>/lib/useCase3Data/TF_PL_ADJ_REPORT.dat', '|', NULL, NULL, 0, 0, 6, 0, 'ImportOra', NULL);
call syscs_util.import_table_ex('APP', 'TF_PL_POSITION_PTD', '<path_prefix>/lib/useCase3Data/TF_PL_POSITION_PTD.dat', '|', NULL, NULL, 0, 0, 6, 0, 'ImportOra', NULL);
call syscs_util.import_table_ex('APP', 'TF_PL_POSITION_YTD', '<path_prefix>/lib/useCase3Data/TF_PL_POSITION_YTD.dat', '|', NULL, NULL, 0, 0, 6, 0, 'ImportOra', NULL);
call syscs_util.import_table_ex('APP', 'TL_SOURCE_SYSTEM', '<path_prefix>/lib/useCase3Data/TL_SOURCE_SYSTEM.dat', '|', NULL, NULL, 0, 0, 6, 0, 'ImportOra', NULL);
call syscs_util.import_table_ex('APP', 'TX_PL_USER_POSN_MAP', '<path_prefix>/lib/useCase3Data/TX_PL_USER_POSN_MAP.dat', '|', NULL, NULL, 0, 0, 6, 0, 'ImportOra', NULL);
