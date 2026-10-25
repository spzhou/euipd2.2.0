/*
 * Author: 周朔鹏
 * Date: 2026-10-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 *
 * 版本显示标签（《版本管理统一方案》第 5.2 节），与后端 VersionLabelUtil 同名同义：
 * 主开发版本 01.02.0003；特性版本 01.02.0003-0002；内容版本 V0.3；
 * 特性版本附内容版本 01.02.0003-0002（V0.3）；CBB 发布版本原样显示。
 */
import { normalizeLegacy, lockedCharacterNo } from '@/utils/dev-version-util';

function hasText(v) {
  return v !== undefined && v !== null && String(v).trim() !== '';
}

/** 主开发版本：能规范的输出 xx.xx.xxxx，否则原样。 */
export function devVersion(raw) {
  if (!hasText(raw)) {
    return '';
  }
  return normalizeLegacy(raw) || String(raw).trim();
}

/** 特性版本：主开发版本-四位特性编号；任一缺失时为空。 */
export function featureVersion(dev, featureNo) {
  return lockedCharacterNo(dev, featureNo) || '';
}

export function contentVersion(label) {
  return hasText(label) ? String(label).trim() : '';
}

/** 特性版本附内容版本；无特性编号时以主开发版本代替特性版本。 */
export function featureWithContent(dev, featureNo, contentLabel) {
  const head = featureVersion(dev, featureNo) || devVersion(dev);
  const content = contentVersion(contentLabel);
  if (!head) {
    return content;
  }
  return content ? head + '（' + content + '）' : head;
}

/** CBB 发布版本不做主开发版本规范，存量 1.00、2.00 原样输出。 */
export function cbbVersion(raw) {
  return hasText(raw) ? String(raw).trim() : '';
}

/** 项目特性列表：锁定版本，最新编号更大时附“最新 X”。 */
export function lockedWithLatest(dev, lockedNo, latestNo) {
  const latest = featureVersion(dev, latestNo);
  const locked = featureVersion(dev, lockedNo);
  if (!locked) {
    return latest;
  }
  if (latest && Number(latestNo) > Number(lockedNo)) {
    return locked + '（最新 ' + latest + '）';
  }
  return locked;
}

const VersionLabel = {
  devVersion,
  featureVersion,
  contentVersion,
  featureWithContent,
  cbbVersion,
  lockedWithLatest,
};

export default VersionLabel;
