/**
 * 任务状态数据访问Mixin
 * 提供向后兼容的访问方式
 */
export default {
  computed: {
    // 任务数量相关
    jobCount: {
      get() { return this.taskCounts.jobCount; },
      set(value) { this.taskCounts.jobCount = value; }
    },
    setupCount: {
      get() { return this.taskCounts.setupCount; },
      set(value) { this.taskCounts.setupCount = value; }
    },
    projectNotifyNum: {
      get() { return this.taskCounts.projectNotifyNum; },
      set(value) { this.taskCounts.projectNotifyNum = value; }
    },
    productNotifyNum: {
      get() { return this.taskCounts.productNotifyNum; },
      set(value) { this.taskCounts.productNotifyNum = value; }
    },
    developNum: {
      get() { return this.taskCounts.developNum; },
      set(value) { this.taskCounts.developNum = value; }
    },
    qualitySubmitNum: {
      get() { return this.taskCounts.qualitySubmitNum; },
      set(value) { this.taskCounts.qualitySubmitNum = value; }
    },
    materialAppNum: {
      get() { return this.taskCounts.materialAppNum; },
      set(value) { this.taskCounts.materialAppNum = value; }
    },
    materialAppNum_project: {
      get() { return this.taskCounts.materialAppNum_project; },
      set(value) { this.taskCounts.materialAppNum_project = value; }
    },
    feeAppNum: {
      get() { return this.taskCounts.feeAppNum; },
      set(value) { this.taskCounts.feeAppNum = value; }
    },
    feeAppNum_project: {
      get() { return this.taskCounts.feeAppNum_project; },
      set(value) { this.taskCounts.feeAppNum_project = value; }
    },
    reviewJobNum: {
      get() { return this.taskCounts.reviewJobNum; },
      set(value) { this.taskCounts.reviewJobNum = value; }
    },
    reviewMilestoneJobNum: {
      get() { return this.taskCounts.reviewMilestoneJobNum; },
      set(value) { this.taskCounts.reviewMilestoneJobNum = value; }
    },
    reviewSubprojectNum: {
      get() { return this.taskCounts.reviewSubprojectNum; },
      set(value) { this.taskCounts.reviewSubprojectNum = value; }
    },
    reviewRelatedJobNum: {
      get() { return this.taskCounts.reviewRelatedJobNum; },
      set(value) { this.taskCounts.reviewRelatedJobNum = value; }
    },
    updateCharacterNum: {
      get() { return this.taskCounts.updateCharacterNum; },
      set(value) { this.taskCounts.updateCharacterNum = value; }
    },
    testManageJobNum: {
      get() { return this.taskCounts.testManageJobNum; },
      set(value) { this.taskCounts.testManageJobNum = value; }
    },
    functionManageJobNum: {
      get() { return this.taskCounts.functionManageJobNum; },
      set(value) { this.taskCounts.functionManageJobNum = value; }
    },
    budgetManageJobNum: {
      get() { return this.taskCounts.budgetManageJobNum; },
      set(value) { this.taskCounts.budgetManageJobNum = value; }
    },
    qualityRecheckNum: {
      get() { return this.taskCounts.qualityRecheckNum; },
      set(value) { this.taskCounts.qualityRecheckNum = value; }
    },
    waitForApproveNum: {
      get() { return this.taskCounts.waitForApproveNum; },
      set(value) { this.taskCounts.waitForApproveNum = value; }
    },
    qualityDefineSubmitNum: {
      get() { return this.taskCounts.qualityDefineSubmitNum; },
      set(value) { this.taskCounts.qualityDefineSubmitNum = value; }
    },
    qualityMaterialApplicationNum: {
      get() { return this.taskCounts.qualityMaterialApplicationNum; },
      set(value) { this.taskCounts.qualityMaterialApplicationNum = value; }
    },
    qualityFeeApplicationNum: {
      get() { return this.taskCounts.qualityFeeApplicationNum; },
      set(value) { this.taskCounts.qualityFeeApplicationNum = value; }
    },

    // 任务标志相关
    characterJobFlag: {
      get() { return this.taskFlags.characterJobFlag; },
      set(value) { this.taskFlags.characterJobFlag = value; }
    },
    characterJobFlag_evaluate: {
      get() { return this.taskFlags.characterJobFlag_evaluate; },
      set(value) { this.taskFlags.characterJobFlag_evaluate = value; }
    },
    demandJobFlag: {
      get() { return this.taskFlags.demandJobFlag; },
      set(value) { this.taskFlags.demandJobFlag = value; }
    },
    developHasList: {
      get() { return this.taskFlags.developHasList; },
      set(value) { this.taskFlags.developHasList = value; }
    },
    demandHasList: {
      get() { return this.taskFlags.demandHasList; },
      set(value) { this.taskFlags.demandHasList = value; }
    },
    preProjectHasList: {
      get() { return this.taskFlags.preProjectHasList; },
      set(value) { this.taskFlags.preProjectHasList = value; }
    },
    projectHasList: {
      get() { return this.taskFlags.projectHasList; },
      set(value) { this.taskFlags.projectHasList = value; }
    },
    testHasList: {
      get() { return this.taskFlags.testHasList; },
      set(value) { this.taskFlags.testHasList = value; }
    },
    setupProjectHasList: {
      get() { return this.taskFlags.setupProjectHasList; },
      set(value) { this.taskFlags.setupProjectHasList = value; }
    },
    reviewHasList: {
      get() { return this.taskFlags.reviewHasList; },
      set(value) { this.taskFlags.reviewHasList = value; }
    },
    productHasList: {
      get() { return this.taskFlags.productHasList; },
      set(value) { this.taskFlags.productHasList = value; }
    },
    qualityHasList: {
      get() { return this.taskFlags.qualityHasList; },
      set(value) { this.taskFlags.qualityHasList = value; }
    },
    materialHasList: {
      get() { return this.taskFlags.materialHasList; },
      set(value) { this.taskFlags.materialHasList = value; }
    },
    jobitemHasList: {
      get() { return this.taskFlags.jobitemHasList; },
      set(value) { this.taskFlags.jobitemHasList = value; }
    }
  }
}
