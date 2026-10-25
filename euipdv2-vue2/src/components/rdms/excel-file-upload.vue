<!--
 * Author: 周朔鹏
 * Date: 2024-01-28
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <button type="button" v-on:click="selectFile()" class="btn" style="width: 150px;" :disabled="saving">
      <i class="ace-icon fa fa-upload"></i>
      {{text}}
    </button>
    <input class="hidden" type="file" ref="file"
           accept=".xlsx,.xls"
           v-on:change="uploadFile()"
           v-bind:id="inputId+'-input'">
  </div>
</template>

<script>
  import Swal from "sweetalert2";

  const MAX_BATCH_SIZE = 500;
  const PREVIEW_ROWS = 5;

  const HEADER_MAP = {
    loginname: 'loginName',
    '登录名': 'loginName',
    '登录名称': 'loginName',
    '手机号': 'loginName',
    '手机号码': 'loginName',
    truename: 'trueName',
    '姓名': 'trueName',
    '真实姓名': 'trueName',
    jobposition: 'jobPosition',
    '职位': 'jobPosition',
    title: 'title',
    '职务': 'title',
    '职称': 'title',
    '职务职称': 'title',
    status: 'status',
    '状态': 'status',
    subject: 'subject',
    '专业': 'subject',
    '学科专业': 'subject',
    educationbackground: 'educationBackground',
    '学历': 'educationBackground',
    '教育背景': 'educationBackground',
    degree: 'degree',
    '学位': 'degree',
    joblevel: 'jobLevel',
    '职级': 'jobLevel',
    '职级代码': 'jobLevel',
    description: 'description',
    '描述': 'description',
    '能力描述': 'description',
    officetel: 'officeTel',
    '办公电话': 'officeTel',
    officeemail: 'officeEmail',
    '邮箱': 'officeEmail',
    '电子邮箱': 'officeEmail',
    '办公邮箱': 'officeEmail',
    officeaddress: 'officeAddress',
    '办公地址': 'officeAddress',
    jobnum: 'jobNum',
    '工号': 'jobNum',
    jointime: 'joinTime',
    '入职时间': 'joinTime',
    '入职日期': 'joinTime'
  };

  const FIELD_LABELS = {
    loginName: '手机号码',
    trueName: '姓名',
    jobPosition: '职位',
    title: '职务',
    status: '状态',
    subject: '专业',
    educationBackground: '学历',
    degree: '学位',
    jobLevel: '职级',
    description: '能力描述',
    officeTel: '办公电话',
    officeEmail: '邮箱',
    officeAddress: '办公地址',
    jobNum: '工号',
    joinTime: '入职时间'
  };

  const EDUCATION_ALIAS = {
    '硕士研究生': '硕士',
    '研究生': '硕士',
    '博士研究生': '博士',
    '大学本科': '本科',
    '专科': '大专',
    '大学专科': '大专'
  };

  // 表头占 Excel 第1行，数据第 index 条对应 Excel 第 index+2 行
  const excelRow = index => index + 2;

  const escapeHtml = value => String(value == null ? '' : value)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;').replace(/'/g, '&#39;');

  // 超出列宽时截断后导入的自由文本字段（与 rdms_customer_user 列宽一致）
  const TRUNCATE_FIELDS = {
    description: 255,
    officeAddress: 255
  };

  const STATUS_ALIAS = {
    '正常': 'NORMAL',
    '在职': 'NORMAL',
    '辞职': 'VACATE',
    '离职': 'VACATE',
    '辞退': 'DISMISS',
    normal: 'NORMAL',
    vacate: 'VACATE',
    dismiss: 'DISMISS'
  };

  export default {
    name: 'excel-file-upload',
    props: {
      text: {
        default: "上传文件"
      },
      inputId: {
        default: "file-upload"
      },
      validationRules: {
        type: Object,
        default: () => ({
          requiredFields: ['loginName', 'trueName', 'jobPosition', 'title', 'status', 'subject', 'educationBackground'],
          fieldRules: {
            loginName: {
              type: 'phone',
              pattern: /^1[3-9]\d{9}$/,
              message: '必须是有效的11位手机号码'
            },
            trueName: {
              type: 'string',
              minLength: 2,
              maxLength: 4,
              message: '真实姓名必须是2-4个字符'
            },
            jobPosition: {
              type: 'string',
              minLength: 1,
              maxLength: 20
            },
            title: {
              type: 'string',
              minLength: 1,
              maxLength: 50
            },
            status: {
              type: 'enum',
              values: ['NORMAL', 'VACATE', 'DISMISS'],
              message: '状态必须是：NORMAL、VACATE、DISMISS之一'
            },
            subject: {
              type: 'string',
              minLength: 2,
              maxLength: 20
            },
            educationBackground: {
              type: 'enum',
              values: ['小学', '初中', '高中', '大专', '本科', '硕士', '博士'],
              message: '教育背景必须是：小学、初中、高中、大专、本科、硕士、博士之一'
            },
            jobLevel: {
              type: 'enum',
              values: ['P3', 'P4', 'P5', 'P6', 'P7', 'P8', 'P9', 'P10', 'P11', 'P12', 'P13', 'P14', 'P15', 'P16', 'P17', 'P18', 'P19', 'P20'],
              message: '职级必须是 P3-P20 之一'
            },
            jobNum: {
              type: 'string',
              maxLength: 10
            },
            degree: {
              type: 'string',
              maxLength: 20
            },
            officeTel: {
              type: 'phone',
              pattern: /^(\d{3,4}-?)?\d{7,8}$/,
              message: '必须是有效的固定电话号码'
            },
            officeEmail: {
              type: 'email',
              maxLength: 100,
              pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
              message: '必须是有效的邮箱地址'
            },
            joinTime: {
              type: 'date',
              message: '无法识别的日期，请使用 Excel 日期格式或 yyyy-mm-dd'
            }
          }
        })
      }
    },
    data: function () {
      return {
        loginUser: {},
        loginCustomer: {},
        sheetContent: [],
        validationErrors: [],
        truncateWarnings: [],
        saving: false
      }
    },

    mounted: function() {
      this.getLoginUser();
    },

    methods: {
      getLoginUser(){
        let _this = this;
        if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
          _this.loginUser = Tool.getLoginUser().adminUser;
          _this.loginCustomer =Tool.getLoginUser();
        }else{
          _this.loginUser = Tool.getLoginUser();
          _this.loginCustomer = Tool.getLoginUser().customer;
        }
      },

      selectFile () {
        if (this.saving) {
          return;
        }
        this.$refs.file.value = "";
        this.$refs.file.click();
      },

      uploadFile () {
        let _this = this;
        let file = _this.$refs.file.files[0];

        if (!file) {
          Toast.error("请选择文件", Swal);
          return;
        }

        const allowedTypes = ['.xlsx', '.xls'];
        const fileExtension = file.name.toLowerCase().substr(file.name.lastIndexOf('.'));

        if (!allowedTypes.includes(fileExtension)) {
          Toast.error("只支持Excel文件格式(.xlsx, .xls)", Swal);
          _this.$refs.file.value = "";
          return;
        }

        const maxSize = 10 * 1024 * 1024;
        if (file.size > maxSize) {
          Toast.error("文件大小不能超过10MB", Swal);
          return;
        }

        Swal.fire({
          title: '正在读取文件...',
          text: '请稍候',
          allowOutsideClick: false,
          didOpen: () => {
            Swal.showLoading();
          }
        });

        _this.readWorkbookFromLocalFile(file, _this.readWorkbook);
      },

      readWorkbookFromLocalFile(file, callback) {
        let _this = this;
        var reader = new FileReader();

        reader.onload = async function (e) {
          try {
            var data = e.target.result;
            const XLSX = await import(/* webpackChunkName: "xlsx" */ 'xlsx');
            var workbook = XLSX.read(data, {type: 'array'});
            if (callback) callback(workbook, XLSX);
          } catch (error) {
            Swal.close();
            console.error('文件读取错误：', error);
            Toast.error("文件格式错误或文件已损坏", Swal);
          }
        };

        reader.onerror = function () {
          Swal.close();
          Toast.error("文件读取失败", Swal);
        };

        reader.readAsArrayBuffer(file);
      },

      mapHeaderKey(rawKey) {
        if (rawKey == null) {
          return null;
        }
        const key = String(rawKey).trim();
        if (!key) {
          return null;
        }
        if (HEADER_MAP[key]) {
          return HEADER_MAP[key];
        }
        const lower = key.toLowerCase().replace(/\s+/g, '');
        if (HEADER_MAP[lower]) {
          return HEADER_MAP[lower];
        }
        // already english field
        const known = [
          'loginName', 'trueName', 'jobPosition', 'title', 'status', 'subject',
          'educationBackground', 'degree', 'jobLevel', 'description',
          'officeTel', 'officeEmail', 'officeAddress', 'jobNum', 'joinTime'
        ];
        const exact = known.find(k => k.toLowerCase() === lower);
        return exact || null;
      },

      normalizeMobile(value) {
        if (value === undefined || value === null || value === '') {
          return '';
        }
        let s = String(value).trim();
        if (/^[0-9.]+[Ee][+-]?[0-9]+$/.test(s)) {
          try {
            s = Number(s).toFixed(0);
          } catch (e) {
            // keep
          }
        }
        if (s.endsWith('.0')) {
          s = s.slice(0, -2);
        }
        // excel numeric phone
        if (/^\d+\.0+$/.test(s)) {
          s = s.split('.')[0];
        }
        return s;
      },

      normalizeCell(field, value) {
        if (value === undefined || value === null) {
          return '';
        }
        if (field === 'loginName') {
          return this.normalizeMobile(value);
        }
        if (field === 'jobLevel') {
          const s = String(value).trim().toUpperCase();
          return s || 'P4';
        }
        if (field === 'status') {
          const raw = String(value).trim();
          if (!raw) {
            return '';
          }
          const mapped = STATUS_ALIAS[raw] || STATUS_ALIAS[raw.toLowerCase()];
          return mapped || raw.toUpperCase();
        }
        if (field === 'educationBackground') {
          const raw = String(value).trim();
          return EDUCATION_ALIAS[raw] || raw;
        }
        if (field === 'joinTime') {
          return this.normalizeDate(value);
        }
        if (typeof value === 'number') {
          return String(value);
        }
        return typeof value === 'string' ? value.trim() : value;
      },

      // Excel 日期序列号或 yyyy-mm-dd / yyyy/mm/dd 文本转为 Date；无法识别时原样返回，由校验报错
      normalizeDate(value) {
        if (value instanceof Date) {
          return value;
        }
        if (typeof value === 'number') {
          const utcDays = Math.round(value - 25569);
          return new Date(utcDays * 86400 * 1000);
        }
        const s = String(value).trim();
        if (!s) {
          return '';
        }
        const m = s.match(/^(\d{4})[-/.年](\d{1,2})[-/.月](\d{1,2})日?$/);
        if (m) {
          const date = new Date(Date.UTC(Number(m[1]), Number(m[2]) - 1, Number(m[3])));
          if (date.getUTCMonth() === Number(m[2]) - 1) {
            return date;
          }
        }
        return s;
      },

      normalizeRows(jsonData, warnings) {
        return jsonData.map((raw, index) => {
          const row = {};
          Object.keys(raw || {}).forEach(k => {
            const field = this.mapHeaderKey(k);
            if (!field) {
              return;
            }
            row[field] = this.normalizeCell(field, raw[k]);
          });
          if (!row.jobLevel) {
            row.jobLevel = 'P4';
          }
          Object.keys(TRUNCATE_FIELDS).forEach(field => {
            const max = TRUNCATE_FIELDS[field];
            const chars = typeof row[field] === 'string' ? Array.from(row[field]) : null;
            if (chars && chars.length > max) {
              row[field] = chars.slice(0, max).join('');
              if (warnings) {
                warnings.push(`第${excelRow(index)}行：${FIELD_LABELS[field]}共${chars.length}个字符，已截断为前${max}个字符`);
              }
            }
          });
          return row;
        });
      },

      readWorkbook(workbook, XLSX) {
        let _this = this;

        try {
          Swal.close();

          if (!workbook || !workbook.Sheets) {
            Toast.error("无效的Excel文件", Swal);
            return;
          }

          var sheetNames = Object.keys(workbook.Sheets);
          if (sheetNames.length === 0) {
            Toast.error("Excel文件中没有找到工作表", Swal);
            return;
          }

          let sheet = workbook.Sheets[sheetNames[0]];
          let jsonData = XLSX.utils.sheet_to_json(sheet);

          if (!jsonData || jsonData.length === 0) {
            Toast.error("Excel文件中没有找到数据", Swal);
            return;
          }

          const headerRow = (XLSX.utils.sheet_to_json(sheet, {header: 1})[0] || []);
          const mappedHeaders = headerRow.map(h => _this.mapHeaderKey(h)).filter(Boolean);
          const missingHeaders = _this.validationRules.requiredFields
            .filter(f => !mappedHeaders.includes(f))
            .map(f => FIELD_LABELS[f] || f);
          if (missingHeaders.length > 0) {
            Swal.fire({
              title: '表头不符合模板',
              html: `第一个工作表“${escapeHtml(sheetNames[0])}”缺少必填列：<b>${escapeHtml(missingHeaders.join('、'))}</b><br/>请下载员工信息表模板，按模板表头填写后重新上传。`,
              icon: 'error',
              confirmButtonText: '我知道了'
            });
            return;
          }

          if (jsonData.length > MAX_BATCH_SIZE) {
            Toast.error(`单次导入不能超过 ${MAX_BATCH_SIZE} 条，当前 ${jsonData.length} 条`, Swal);
            return;
          }

          const truncateWarnings = [];
          const workers = _this.normalizeRows(jsonData, truncateWarnings);
          _this.sheetContent = workers;
          _this.truncateWarnings = truncateWarnings;
          const warningHtml = truncateWarnings.length === 0 ? '' : `
                <p style="margin-top:10px;color:#e67e22;"><strong>以下内容超长，将截断后导入：</strong></p>
                <div style="font-size:13px;color:#e67e22;line-height:1.6;">${truncateWarnings.map(escapeHtml).join('<br/>')}</div>`;

          const preview = workers.slice(0, PREVIEW_ROWS).map((w, i) =>
            `${i + 1}. ${escapeHtml(w.loginName || '-')} / ${escapeHtml(w.trueName || '-')} / ${escapeHtml(w.jobPosition || '-')}`
          ).join('<br/>');

          Swal.fire({
            title: '数据预览',
            html: `
              <div style="text-align: left;">
                <p><strong>工作表：</strong>${escapeHtml(sheetNames[0])}</p>
                <p><strong>将导入行数：</strong>${workers.length}</p>
                <p><strong>说明：</strong>已存在同机构手机号将更新，否则新建；任一行失败则整批不落库。</p>
                <p style="margin-top:10px;"><strong>前 ${Math.min(PREVIEW_ROWS, workers.length)} 行摘要：</strong></p>
                <div style="font-size:13px;color:#555;line-height:1.6;">${preview}</div>${warningHtml}
              </div>
            `,
            icon: 'info',
            showCancelButton: true,
            confirmButtonText: '开始导入',
            cancelButtonText: '取消',
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33'
          }).then((result) => {
            if (result.isConfirmed) {
              _this.saveWorkers(workers);
            }
          });

        } catch (error) {
          Swal.close();
          console.error('数据解析错误：', error);
          Toast.error("数据解析失败", Swal);
        }
      },

      validateField(value, field, rule) {
        const errors = [];
        const fieldName = FIELD_LABELS[field] || field;
        let v = value;

        if (rule.type === 'date') {
          if (!(v instanceof Date) || isNaN(v.getTime())) {
            errors.push(`${fieldName}“${v}”${rule.message}`);
          }
          return errors;
        }

        if (rule.type === 'string') {
          if (typeof v !== 'string') {
            v = String(v || '');
          }
          if (rule.minLength && v.length < rule.minLength) {
            errors.push(`${fieldName}不能少于${rule.minLength}个字符（当前${v.length}个）`);
          }
          if (rule.maxLength && v.length > rule.maxLength) {
            errors.push(`${fieldName}不能超过${rule.maxLength}个字符（当前${v.length}个）`);
          }
          if (rule.pattern && !rule.pattern.test(v)) {
            errors.push(rule.message || `${fieldName}格式不正确`);
          }
        }

        if (rule.type === 'enum') {
          if (!rule.values.includes(v)) {
            errors.push(`${rule.message || fieldName + '值不在允许范围内'}（当前为“${v}”）`);
          }
        }

        if (rule.type === 'phone') {
          if (typeof v !== 'string') {
            v = String(v || '');
          }
          if (rule.pattern && !rule.pattern.test(v)) {
            errors.push(`${fieldName}${rule.message || '格式不正确'}（当前为“${v}”）`);
          }
        }

        if (rule.type === 'email') {
          if (typeof v !== 'string') {
            v = String(v || '');
          }
          if (rule.maxLength && v.length > rule.maxLength) {
            errors.push(`${fieldName}不能超过${rule.maxLength}个字符（当前${v.length}个）`);
          } else if (rule.pattern && !rule.pattern.test(v)) {
            errors.push(`${rule.message || fieldName + '格式不正确'}（当前为“${v}”）`);
          }
        }

        return errors;
      },

      validateRow(rowData, rowIndex) {
        let _this = this;
        const errors = [];
        const rules = _this.validationRules;

        rules.requiredFields.forEach(field => {
          const value = rowData[field];
          if (value === undefined || value === null || (typeof value === 'string' && value.trim() === '')) {
            errors.push({
              row: rowIndex + 1,
              field: field,
              value: value,
              message: `第${excelRow(rowIndex)}行：${FIELD_LABELS[field] || field}为必填字段`
            });
          }
        });

        Object.keys(rules.fieldRules).forEach(field => {
          const value = rowData[field];
          const rule = rules.fieldRules[field];
          if (value !== undefined && value !== null && value !== '') {
            const fieldErrors = _this.validateField(value, field, rule);
            fieldErrors.forEach(error => {
              errors.push({
                row: rowIndex + 1,
                field: field,
                value: value,
                message: `第${excelRow(rowIndex)}行：${error}`
              });
            });
          }
        });

        return errors;
      },

      validateAllData(workerList) {
        let _this = this;
        _this.validationErrors = [];
        const validWorkers = [];

        if (!workerList || workerList.length === 0) {
          _this.validationErrors.push({
            row: 0,
            field: 'general',
            message: '没有找到有效数据'
          });
          return { valid: false, workers: [], errors: _this.validationErrors };
        }

        if (workerList.length > MAX_BATCH_SIZE) {
          _this.validationErrors.push({
            row: 0,
            field: 'general',
            message: `单次导入不能超过 ${MAX_BATCH_SIZE} 条，当前 ${workerList.length} 条`
          });
          return { valid: false, workers: [], errors: _this.validationErrors };
        }

        const loginNameFirstRow = {};
        workerList.forEach((worker, index) => {
          const rowErrors = _this.validateRow(worker, index);
          const loginName = worker.loginName;
          if (loginName) {
            if (loginNameFirstRow[loginName] != null) {
              rowErrors.push({
                row: index + 1,
                field: 'loginName',
                message: `第${excelRow(index)}行：手机号码与第${loginNameFirstRow[loginName]}行重复`
              });
            } else {
              loginNameFirstRow[loginName] = excelRow(index);
            }
          }

          if (rowErrors.length === 0) {
            validWorkers.push(worker);
          } else {
            _this.validationErrors.push(...rowErrors);
          }
        });

        return {
          valid: _this.validationErrors.length === 0,
          workers: validWorkers,
          errors: _this.validationErrors
        };
      },

      saveWorkers(workerList) {
        let _this = this;
        const validationResult = _this.validateAllData(workerList);

        if (!validationResult.valid) {
          _this.showValidationErrors(validationResult.errors);
          return;
        }

        if (validationResult.workers.length > 0) {
          _this.saveWorkerList(validationResult.workers);
        } else {
          Toast.warning("没有有效的数据可以保存", Swal);
        }
      },

      showValidationErrors(errors) {
        if (!errors || errors.length === 0) {
          return;
        }

        const errorsByRow = {};
        errors.forEach(error => {
          if (!errorsByRow[error.row]) {
            errorsByRow[error.row] = [];
          }
          errorsByRow[error.row].push(error.message);
        });

        let errorHtml = '<div style="text-align: left; max-height: 400px; overflow-y: auto;">';
        errorHtml += `<h4>共 ${errors.length} 处问题，整批未导入，请检查：</h4>`;
        errorHtml += '<ul style="margin: 10px 0; padding-left: 20px;">';

        Object.keys(errorsByRow).forEach(row => {
          errorsByRow[row].forEach(message => {
            errorHtml += `<li style="margin: 5px 0; color: #e74c3c;">${escapeHtml(message)}</li>`;
          });
        });

        errorHtml += '</ul>';
        errorHtml += '<p style="margin-top: 15px; color: #666; font-size: 14px;">提示：请修正Excel后重新上传</p>';
        errorHtml += '</div>';

        Swal.fire({
          title: '数据验证失败',
          html: errorHtml,
          icon: 'error',
          showCancelButton: false,
          confirmButtonText: '我知道了',
          confirmButtonColor: '#3085d6',
          width: '600px'
        });
      },

      saveWorkerList(workers){
        let _this = this;
        workers.forEach(item => {
          item.customerId = _this.loginCustomer.id;
        });
        _this.saving = true;
        Swal.fire({
          title: '正在导入...',
          text: '请稍候，校验通过后才会写入',
          allowOutsideClick: false,
          didOpen: () => {
            Swal.showLoading();
          }
        });

        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/saveWorkers', workers).then((response)=>{
          _this.saving = false;
          Swal.close();
          let resp = response.data;
          if (resp.success) {
            const content = resp.content || {};
            const created = content.created != null ? content.created : 0;
            const updated = content.updated != null ? content.updated : 0;
            const warnings = Array.from(new Set(
              _this.truncateWarnings.concat((content.warnings || []).map(w => w.message))));
            if (warnings.length > 0) {
              Swal.fire({
                title: `导入成功：新建 ${created}，更新 ${updated}`,
                html: `<div style="text-align:left;font-size:13px;line-height:1.6;color:#e67e22;">以下内容超长，已截断后导入：<br/>${warnings.map(escapeHtml).join('<br/>')}</div>`,
                icon: 'success',
                confirmButtonText: '我知道了'
              });
            } else {
              Toast.success(`导入成功：新建 ${created}，更新 ${updated}`, Swal);
            }
            _this.$emit('import-success', { created, updated, total: content.total });
          } else {
            const content = resp.content || {};
            if (content.errors && content.errors.length) {
              _this.showValidationErrors(content.errors);
            } else {
              Toast.warning(resp.message || '导入失败', Swal);
            }
          }
        }).catch((error) => {
          _this.saving = false;
          Swal.close();
          console.error('保存失败：', error);
          const resp = error && error.response;
          const detail = resp
            ? `服务器返回 ${resp.status}${resp.data && resp.data.message ? '：' + resp.data.message : ''}`
            : '网络异常或服务未启动';
          Toast.error(`保存失败（${detail}），整批未导入，请稍后重试`, Swal);
        });
      }

    }
  }
</script>
