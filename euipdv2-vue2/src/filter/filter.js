/*
 * Author: 周朔鹏
 * Date: 2025-03-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 */

/** 轻量日期格式化，替代 moment，减小主包体积 */
function pad2(n) {
    return n < 10 ? '0' + n : '' + n;
}
function formatDate(value, withTime) {
    if (value === null || value === undefined || value === '') {
        return '';
    }
    const d = value instanceof Date ? value : new Date(value);
    if (isNaN(d.getTime())) {
        return '';
    }
    const date = d.getFullYear() + '-' + pad2(d.getMonth() + 1) + '-' + pad2(d.getDate());
    if (!withTime) {
        return date;
    }
    return date + ' ' + pad2(d.getHours()) + ':' + pad2(d.getMinutes()) + ':' + pad2(d.getSeconds());
}

/**
 * 数组过滤器 例如：{{SECTION_CHARGE | optionKV(section.charge)}}
 * @param object 例如：{CHARGE:{key:"C", value:"收费"},FREE:{key:"F", value:"免费"}}
 * @param key 例如：C
 * @returns {string} 例如：收费
 */
export let optionKV = (object, key) =>  {
    if (!object || !key) {
        return "";
    } else {
        let result = "";
        for(let enums in object){
            if (key === object[enums]["key"]) {
                result = object[enums]["value"];
            }
        }
        return result;
    }
};

/**
 * 数组过滤器 例如：{{SECTION_CHARGE | optionKN(section.charge)}}
 * @param object 例如：SECTION_CHARGE = {CHARGE:{key:"C", value:"收费" , num: 2},FREE:{key:"F", value:"免费", num: 4}}
 * @param key 例如：C
 * @returns {string} 例如：2
 */
export let optionKN = (object, key) =>  {
    if (!object || !key) {
        return "";
    } else {
        let result = "";
        for(let enums in object){
            if (key === object[enums]["key"]) {
                result = object[enums]["num"];
            }
        }
        return result;
    }
};


/**
 * 数组过滤器 例如：{{CHARGE | optionKVArray(section.charge)}}
 * @param list 例如：[{key:"C", value:"收费"},{key:"F", value:"免费"}]
 * @param key 例如：C
 * @returns {string} 例如：收费
 */
let optionKVArray = (list, key) =>  {
    if (!list || !key) {
        return "";
    } else {
        let result = "";
        for (let i = 0; i < list.length; i++) {
            if (key === list[i]["key"]) {
                result = list[i]["value"];
            }
        }
        return result;
    }
};

/**
 * 日期格式化, 例如:{{ worker.joinTime | dateFormat }}
 */

export let dateFormat = (value) => {
    return formatDate(value, true);
};

/**
 * 日期格式化, 例如:{{ worker.joinTime | dateFormat }}
 */

export let shortDateFormat = (value) => {
    return formatDate(value, false);
};

/**
 * 退料单的操作状态以 RETURN_ 开头；原领料单发起退料后为 RETURN，不属于退料单
 */
export let isReturnSlip = (row) => {
    return !!row && typeof row.opeStatus === 'string' && row.opeStatus.indexOf('RETURN_') === 0;
};

/**
 * 领料、退料单状态文字：待物料管理员确认时显示“待发料”或“待收料”，确认后在审批状态后注明
 */
export let materialStatusLabel = (row) => {
    if (!row) {
        return '';
    }
    let text = optionKV(APPLICATION_STATUS, row.status);
    let returning = isReturnSlip(row);
    if (row.pendingIssue) {
        return returning ? '待收料' : '待发料';
    }
    if (row.issueStatus === 'ISSUED'
        && (row.status === APPLICATION_STATUS.APPROVED.key || row.status === APPLICATION_STATUS.PRE_APPROVED.key)) {
        return text + (returning ? '（已收料）' : '（已发料）');
    }
    return text;
};

/**
 * 人民币数据格式化（合法 0 正常显示；null/空/非数字回退 ¥0.00）
 */
export let rmbFormat = (value = 0.00) => {
    const format = (num) => num.toLocaleString("zh-CN", {
        style: "currency",
        currency: "CNY",
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    });
    if (value == null || value === '') {
        return format(0);
    }
    const valueNum = Number(value);
    if (!Number.isFinite(valueNum)) {
        return format(0);
    }
    return format(valueNum);
};

/**
 * 保留两位小数
 */

export let fixed2fractional = (value = 0) => {
    // return value.toFixed(2);
    if(value === null){
        return 0;
    }
    if (!isNaN(value) && typeof value === 'number') {
        // value 是数字
        return value.toFixed(2);
    }else{
        return Number(value).toFixed(2);
    }
};

/**
 * 保留6位小数
 */

export let fixed4fractional = (value = 0) => {
    if(value === null){
        return 0;
    }
    if (!isNaN(value) && typeof value === 'number') {
        // value 是数字
        return value.toFixed(4);
    }else{
        return Number(value).toFixed(4);
    }
};

/**
 * 对迭代号的显示进行格式化
 * @param value
 * @returns {string}
 */
export let iterationFormat = (value = 0) =>{
    if (value === null || value === undefined || value === '') {
        return '';
    }
    const n = Number(value);
    if (!Number.isFinite(n) || n < 0) {
        return '';
    }
    const i = Math.floor(n);
    if (i > 9999) {
        return String(i);
    }
    return String(i).padStart(4, '0');
};

/**
 * 时长格式化
 * @param value 例如：36000
 * @returns {string} 例如：10:00:00
 */
export let formatSecond = (value) => {
    // debugger
    value = value || 0;
    let second = parseInt(value, 10); // 秒
    let perFix = "";
    if(second > 0){
        perFix = "余: ";
    }else{
        perFix = "逾: ";
        second = Math.abs(second);
    }
    let minute = 0; // 分
    let hour = 0; // 小时
    let day = 0; //天
    if (second >= 60) {
        // 当大于60秒时，才需要做转换
        minute = Math.floor(second / 60);
        second = Math.floor(second % 60);
        if (minute >= 60) {
            hour = Math.floor(minute / 60);
            minute = Math.floor(minute % 60);
            if(hour >= 24){
                day = Math.floor(hour / 24);
                hour = Math.floor(hour % 24);
            }
        }
    } else {
        // 小于60秒时，直接显示，不需要处理
    }
    let result = "" + PrefixInteger(second, 2) + "";
    // 拼上分钟
    result = "" + PrefixInteger(minute, 2) + ":" + result;
    // 拼上小时
    result = "" + PrefixInteger(hour, 2) + ":" + result;
    // 拼上天
    if(day > 0){
        result = "" + day + "天" + result;
    }

    return perFix + result;
};

/**
 * 时长格式化
 * @param value 例如：36000
 * @returns {string} 例如：10:00:00
 */
export let formatDuration = (value) => {
    // debugger
    value = value || 0;
    let second = parseInt(value, 10); // 秒
    let minute = 0; // 分
    let hour = 0; // 小时
    let day = 0; //天
    if (second >= 60) {
        // 当大于60秒时，才需要做转换
        minute = Math.floor(second / 60);
        second = Math.floor(second % 60);
        if (minute >= 60) {
            hour = Math.floor(minute / 60);
            minute = Math.floor(minute % 60);
            if(hour >= 24){
                day = Math.floor(hour / 24);
                hour = Math.floor(hour % 24);
            }
        }
    } else {
        // 小于60秒时，直接显示，不需要处理
    }
    let result = "" + PrefixInteger(second, 2) + "";
    // 拼上分钟
    result = "" + PrefixInteger(minute, 2) + ":" + result;
    // 拼上小时
    result = "" + PrefixInteger(hour, 2) + ":" + result;
    // 拼上天
    if(day > 0){
        result = "" + day + "天" + result;
    }

    return result;
};

/**
 * 格式化指定长度，前面补0
 */
function PrefixInteger(num, length) {
    return (Array(length).join('0') + num).slice(-length);
}

/**
 * 格式化文件大小
 * @param value
 * @returns {string}
 */
let formatFileSize = (value) => {
    value = value || 0;
    let result;
    if (value > 100 * 1024) {
        result = Math.round((value / 1024 / 1024) * 100) / 100 + "MB";
    } else {
        result = Math.round((value / 1024) * 100) / 100 + "KB";
    }
    return result;
};

export default {
    optionKV,
    formatSecond,
    formatFileSize,
    dateFormat,
    shortDateFormat,
    fixed2fractional,
    rmbFormat,
    iterationFormat,
    formatDuration,
}
