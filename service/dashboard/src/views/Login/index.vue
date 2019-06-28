<template>
  <div class="welcomeSplash">
    <h2>Grakn Benchmark</h2>
    <a href="https://github.com/login/oauth/authorize?scope=user:email&client_id=ae839e841f2aa0d0bf2d">
      <div class="githubLoginBtn">
        <font-awesome-icon :icon="['fab', 'github']" />
        <span>Sign in with GitHub</span>
      </div>
    </a>
  </div>
</template>

<style lang="scss" scoped>
.welcomeSplash {
  position: absolute;
  top: 50%;
  left: 50%;
  margin: -80px 0 0 -91px;

  text-align: center;

  h2 {
    font-family: "Titillium-Lt";
    text-align: center;

    margin-bottom: 20px;
  }

  .githubLoginBtn {
    width: 182px;
    height: 35px;

    background-color: #444;
    border-radius: 4px;
    color: #fff;
    line-height: 35px;
    text-align: center;

    margin: 0 auto;

    span {
      font-size: 15px;
      margin-left: 9px;
    }

    &:hover {
      background-color: #2b2b2b;
    }
  }
}
</style>
