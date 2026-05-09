<template>  <!--这是单行注释-->
  <NavBar v-if="isLogin" />
  <router-view ></router-view>
</template> // 用来写HTML，这里定义了组件的模板部分，使用了 Vue 的插值语法 {{ }} 来显示 bot_name 和 bot_rating 的值。

<script>  // 单行注释
// 这里的 script 标签用来定义组件的逻辑部分，包括数据、方法等。我们使用了 Vue 3 的 Composition API 来定义组件的 setup 函数
//是用来写js的

import NavBar from "@/components/NabBar.vue";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap"

export default
{
  components: {
    NavBar
  },
  data() {
    return {
      isLogin: false
    }
  },
  watch: {
    $route() {
      this.isLogin = localStorage.getItem('isLogin') === 'true';
    }
  },
  mounted() {
    this.isLogin = localStorage.getItem('isLogin') === 'true';
  }
}
</script> 

// 这里的 style 标签可以用来添加组件的样式,目前是空的，可以根据需要添加 CSS 样式来美化组件的外观。
<style>     /*单行注释*/

body{
  background-image: url("@/assets/images/IMG_1637.JPG");   /* 设置背景图片 */
  background-size: cover;  /* 设置背景图片覆盖整个页面 */
}
</style>
