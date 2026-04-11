INSERT INTO mail_scenario_variables (mail_scenario_id, variable)
SELECT ms.id, 'RECRUIT_NAME'
FROM mail_scenario ms
WHERE ms.scenario_code = 'MEMBER_REJECT_NOTICE'
  AND NOT EXISTS (
    SELECT 1
    FROM mail_scenario_variables msv
    WHERE msv.mail_scenario_id = ms.id
      AND msv.variable = 'RECRUIT_NAME'
);
