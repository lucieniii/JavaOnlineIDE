<template>
  <div id="app" class="container">
    <h4>在线IDE</h4>
    <div class="row">
      <div class="btn col s1" @click="run">运行</div>
      <input class="col s5" placeholder="标准输入(stdin)">
      <ul id="language" class="dropdown-content">
        <li><a href="#!">Python3.6</a></li>
      </ul>
      <input type="number" class="col s1" placeholder="时间限制(ms)" v-model="tle">
      <input type="number" class="col s1" placeholder="空间限制(kb)" v-model="mle">
<!--      v-model 双向绑定-->
      <a class="btn dropdown-trigger col s4" data-target="language">选择语言</a>
    </div>
    <div class="row">
      <div class="col s7">
        <div class="card">
          <div class="card-content">
            <div class="row">
              <textarea rows="30" cols="1" readonly="readonly" class="col s1"></textarea>
              <textarea rows="30" v-model="code" class="col s11"></textarea>
            </div>
          </div>
        </div>
      </div>
      <div class="col s5">
        <div class="card">
          <div class="card-content">
            <textarea rows="30" readonly="readonly" v-model="display"></textarea>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import M from 'materialize-css'
import axios from 'axios'

export default {
  name: 'App',
  data () {
    return {
      code: '',
      display: '',
      tle: '',
      mle: ''
    }
  },
  mounted () {
    M.AutoInit()
  },
  methods: {
    run () {
      this.display = '代码运行中...'
      axios.get('/api/run', {
        params: {
          code: encodeURIComponent(this.code),
          language: 'python3.6',
          password: 'aikxNo.1',
          tle: this.tle,
          mle: this.mle
        }
      }).then((response) => {
        console.log(response.data)
        this.display = response.data.result || response.data.error
      }).catch((err) => {
        console.log(err)
        this.display = '错误：' + err
      })
    }
  }
}
</script>

<style>
  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    height: 100%;
  }

  textarea {
    font-family: Consolas, "Courier New", Monospaced, 微软雅黑, monospace !important;
    height: auto !important;
    resize: none !important;
  }
</style>
