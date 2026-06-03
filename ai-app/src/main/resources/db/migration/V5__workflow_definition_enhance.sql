-- V5: Workflow definition enhancements

-- Add unique constraint on (app_id, workflow_name)
CREATE UNIQUE INDEX IF NOT EXISTS idx_workflow_definition_app_name
    ON workflow_definition(app_id, workflow_name);

-- Add index on app_id
CREATE INDEX IF NOT EXISTS idx_workflow_definition_app_id
    ON workflow_definition(app_id);
