/**
 * 内容文档章节定义，与后端 RichDocSchemaRegistry 保持一致：code 为章节编码，field 为业务 DTO 字段。
 */
const CHARACTER = [
  { code: 'FUNCTION_DESC', name: '特性描述', field: 'functionDescription' },
  { code: 'WORK_CONDITION', name: '生效条件/应用范围', field: 'workCondition' },
  { code: 'INPUT_LOGIC', name: '输入逻辑/使用条件', field: 'inputLogicalDesc' },
  { code: 'FUNCTION_LOGIC', name: '功能逻辑/功能要求', field: 'functionLogicalDesc' },
  { code: 'OUTPUT_LOGIC', name: '输出逻辑/交付指标', field: 'outputLogicalDesc' },
  { code: 'TEST_METHOD', name: '验证方法/测试方法', field: 'testMethod' }
]

function qualityLike (descCode, descName, descField, analysisName) {
  return [
    { code: descCode, name: descName, field: descField },
    { code: 'TRIGGER_CONDITION', name: '触发条件/问题现象', field: 'triggerCondition' },
    { code: 'ESSENTIAL_ANALYSIS', name: analysisName, field: 'essentialAnalysis' },
    { code: 'SOLUTION', name: '解决方案/处理措施', field: 'solution' },
    { code: 'OUTPUT_INDICATOR', name: '输出逻辑/交付指标', field: 'outputIndicator' },
    { code: 'TEST_METHOD', name: '验证方法/测试方法', field: 'testMethod' },
    { code: 'FEEDBACK_DESC', name: '反馈描述', field: 'feedbackDescription' }
  ]
}

export const RICH_SECTIONS = {
  CHARACTER_DEF: CHARACTER,
  CBB_DEF: CHARACTER,
  DEMAND: [
    { code: 'DEMAND_DESC', name: '需求描述', field: 'demandDescription' },
    { code: 'WORK_CONDITION', name: '生效条件或使用前提', field: 'workCondition' }
  ],
  QUALITY: qualityLike('QUALITY_DESC', '问题描述', 'qualityDescription', '质量问题本质分析'),
  BUG: qualityLike('BUG_DESC', '缺陷描述', 'bugDescription', '缺陷问题本质分析'),
  JOBITEM_PROPERTY: [
    { code: 'JOB_DESC', name: '工作描述', field: 'jobDescription' },
    { code: 'TEST_KEY_POINTS', name: '测试/验收关键点说明', field: 'testKeyPoints' }
  ]
}

export function sectionsOf (bizType) {
  return RICH_SECTIONS[bizType] || []
}
