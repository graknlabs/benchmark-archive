/* eslint-disable no-unused-vars */
/* eslint-disable no-shadow */
import Loading from './loading.vue';
import {
  addClass,
  removeClass,
  getStyle,
} from './util/dom';
import afterLeave from './util/after-leave';

const loadingDirective = {};
loadingDirective.install = (Vue, options) => {
  const Mask = Vue.extend(Loading);
  const insertDom = (parent, el, binding) => {
    const options = binding.value;
    if (!el.domVisible
      && getStyle(el, 'display') !== 'none'
      && getStyle(el, 'visibility') !== 'hidden'
    ) {
      Object.keys(el.maskStyle).forEach((property) => {
        el.mask.style[property] = el.maskStyle[property];
      });

      if (el.originalPosition !== 'absolute' && el.originalPosition !== 'fixed') {
        addClass(parent, 'loading-parent--relative');
      }
      if (options.fullscreen) {
        addClass(parent, 'loading-parent--hidden');
      }
      el.domVisible = true;

      parent.appendChild(el.mask);
      Vue.nextTick(() => {
        if (el.instance.hiding) {
          el.instance.$emit('after-leave');
        } else {
          el.instance.visible = true;
        }
      });
      el.domInserted = true;
    }
  };
  const toggleLoading = (el, binding) => {
    const options = binding.value;
    if (options.show) {
      Vue.nextTick(() => {
        if (options.fullscreen) {
          el.originalPosition = getStyle(document.body, 'position');
          el.originalOverflow = getStyle(document.body, 'overflow');
          el.maskStyle.zIndex = options.zIndex;
          addClass(el.mask, 'is-fullscreen');
          insertDom(document.body, el, binding);
        } else {
          removeClass(el.mask, 'is-fullscreen');
          el.originalPosition = getStyle(el, 'position');
          insertDom(el, el, binding);
        }
      });
    } else {
      afterLeave(
        el.instance,
        () => {
          const options = binding.value;
          el.domVisible = false;
          const target = options.fullscreen ? document.body : el;
          removeClass(target, 'loading-parent--relative');
          removeClass(target, 'loading-parent--hidden');
          el.instance.hiding = false;
        },
        300,
        true,
      );
      el.instance.visible = false;
      el.instance.hiding = true;
    }
  };

  Vue.directive('loading', {
    bind(el, binding, vnode) {
      const options = binding.value;
      const mask = new Mask({
        el: document.createElement('div'),
        data: {
          text: options.text || '',
          background: options.background || '',
          customClass: options.customClass || '',
          fullscreen: options.fullscreen || false,
        },
      });
      el.instance = mask;
      el.mask = mask.$el;
      el.maskStyle = {};
      if (options.show) {
        toggleLoading(el, binding);
      }
    },

    update(el, binding) {
      const options = binding.value;
      el.instance.setText(options.text);
      toggleLoading(el, binding);
    },

    unbind(el, binding) {
      if (el.domInserted) {
        if (el.mask && el.mask.parentNode) {
          el.mask.parentNode.removeChild(el.mask);
        }
        toggleLoading(el, {
          value: false,
          modifiers: binding,
        });
      }
    },
  });
};

export default loadingDirective;
