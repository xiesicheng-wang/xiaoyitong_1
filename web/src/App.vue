<template>
  <router-view />
</template>

<script setup>
// 全局只有一个路由出口，页面结构在各个 view 里
</script>

<style>
/* 全局底色由 styles/theme.css 提供（浅粉渐变），此处不再覆盖 */
body {
  margin: 0;
  font-family: "Helvetica Neue", Arial, "PingFang SC", "Microsoft YaHei", sans-serif;
}
</style>
