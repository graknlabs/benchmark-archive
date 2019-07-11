/* eslint-disable func-names */
/* eslint-disable no-param-reassign */
// eslint-disable-next-line no-useless-escape
const SPECIAL_CHARS_REGEXP = /([\:\-\_]+(.))/g;
const MOZ_HACK_REGEXP = /^moz([A-Z])/;

const trim = function (string) {
  return (string || '').replace(/^[\s\uFEFF]+|[\s\uFEFF]+$/g, '');
};

const camelCase = function (name) {
  return name
    .replace(SPECIAL_CHARS_REGEXP, (_, separator, letter, offset) => {
      if (offset) {
        return letter.toUpperCase();
      }
      return letter;
    })
    .replace(MOZ_HACK_REGEXP, 'Moz$1');
};

export function hasClass(el, cls) {
  if (!el || !cls) return false;
  if (cls.indexOf(' ') !== -1) {
    throw new Error('className should not contain space.');
  }
  if (el.classList) {
    return el.classList.contains(cls);
  }
  return ` ${el.className} `.indexOf(` ${cls} `) > -1;
}

export function addClass(el, cls) {
  if (!el) return;
  let curClass = el.className;
  const classes = (cls || '').split(' ');

  for (let i = 0, j = classes.length; i < j; i += 1) {
    const clsName = classes[i];
    // if (!clsName) continue;

    if (el.classList) {
      el.classList.add(clsName);
    } else if (!hasClass(el, clsName)) {
      curClass += ` ${clsName}`;
    }
  }
  if (!el.classList) {
    el.className = curClass;
  }
}

export function removeClass(el, cls) {
  if (!el || !cls) return;
  const classes = cls.split(' ');
  let curClass = ` ${el.className} `;

  for (let i = 0, j = classes.length; i < j; i += 1) {
    const clsName = classes[i];
    // if (!clsName) continue;

    if (el.classList) {
      el.classList.remove(clsName);
    } else if (hasClass(el, clsName)) {
      curClass = curClass.replace(` ${clsName} `, ' ');
    }
  }
  if (!el.classList) {
    el.className = trim(curClass);
  }
}

export const getStyle = function (element, styleName) {
  if (!element || !styleName) return null;
  styleName = camelCase(styleName);
  if (styleName === 'float') {
    styleName = 'cssFloat';
  }
  try {
    const computed = document.defaultView.getComputedStyle(element, '');
    return element.style[styleName] || computed ? computed[styleName] : null;
  } catch (e) {
    return element.style[styleName];
  }
};

export function setStyle(element, styleName, value) {
  if (!element || !styleName) return;

  if (typeof styleName === 'object') {
    Object.keys(styleName).forEach((key) => {
      if (Object.prototype.hasOwnProperty.call(styleName, key)) {
        setStyle(element, key, styleName[key]);
      }
    });
  } else {
    styleName = camelCase(styleName);
    element.style[styleName] = value;
  }
}
