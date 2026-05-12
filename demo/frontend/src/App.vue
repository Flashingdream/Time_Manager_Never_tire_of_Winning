<template>
  <!-- 背景图层 -->
  <div class="bg-overlay"></div>

  <NavBar v-if="isLogin" />
  <router-view></router-view>
  <ReminderOverlay />
</template>

<script>
import NavBar from "@/components/NabBar.vue";
import ReminderOverlay from "@/components/ReminderOverlay.vue";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap"

export default
{
  components: {
    NavBar,
    ReminderOverlay
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

    // 应用自定义背景设置
    const savedBg = localStorage.getItem('bgImage');
    const savedOpacity = localStorage.getItem('bgOpacity');
    const overlay = document.querySelector('.bg-overlay');
    if (overlay) {
      if (savedBg) {
        overlay.style.backgroundImage = `url(${savedBg})`;
      }
      const opacity = savedOpacity ? parseInt(savedOpacity) / 100 : 1;
      overlay.style.opacity = opacity.toString();
    }
  }
}
</script>

<style>
body {
  background-image: url("@/assets/images/IMG_1637.JPG");
  background-size: cover;
  background-attachment: fixed;
  background-position: center;
}

/* 背景图层：用于自定义背景 + 透明度 */
.bg-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-attachment: fixed;
  background-position: center;
  background-repeat: no-repeat;
  z-index: -1;
  pointer-events: none;
}
</style>
