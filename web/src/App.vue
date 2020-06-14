<template>
  <div id="app" class="container">
    <h4>在线IDE</h4>
    <div class="row">
      <div class="input-field col s4">
        <select v-model="language">
          <option value="python3.6" selected>Python(3.8.3)</option>
          <option value="g++">C++(g++8.3.0)</option>
          <option value="gcc">C(gcc8.3.0)</option>
          <option value="java8">Java(jdk8)</option>
          <option value="java12">Java(jdk12)</option>
        </select>
        <label>选择语言与编译器</label>
      </div>
      <div class="input-field col s2 offset-s1" id="tle">
        <input class="validate" type="number" v-model="tle" min="0" max="30">
        <label for="tle">时间限制(s)</label>
      </div>
      <div class="input-field col s2 offset-s1" id="mle">
        <input class="validate" type="number" v-model="mle" min="0" max="65534">
        <label for="mle">空间限制(KB)</label>
      </div>
      <div class="input-field btn col s1 offset-s1" @click="run">运行</div>
      <div class="input-field col s8">
        <input type="text" id="stdin">
        <label for="stdin">标准输入(stdin)</label>
      </div>
      <div class="file-field input-field col s4">
        <div class="row">
          <div class="file-path-wrapper col s7">
            <input class="file-path validate" type="text" id="file" placeholder="比对文件名">
          </div>
          <div class="btn col s4 offset-s1">
            <span>选择比对文件</span>
            <input type="file">
          </div>
        </div>
      </div>
      <div class="col s7">
        <div class="card">
          <div class="card-content">
            <textarea rows="25" id="code" v-model="code" @keydown.tab="tab($event)"></textarea>
          </div>
        </div>
      </div>
      <div class="col s5">
        <div class="card">
          <div class="card-content">
            <textarea rows="25" readonly="readonly" v-model="display"></textarea>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!--      <html>-->
  <!--      <head>-->
  <!--        <meta http-equiv="Content-type" content="text/html; charset=UTF-8">-->
  <!--        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>-->
  <!--        <title>单文件上传</title>-->
  <!--      </head>-->
  <!--      <body>-->
  <!--      <form method="post" action="/api" enctype="multipart/form-data">-->
  <!--        <input type="file" name="file"><br>-->
  <!--        <input type="submit" value="提交">-->
  <!--      </form>-->
  <!--      </body>-->
  <!--      </html>-->
</template>

<script>
import M from 'materialize-css'
import axios from 'axios'

export default {
  name: 'App',
  data () {
    return {
      language: 'python3.6',
      tle: '',
      mle: '',
      stdin: '',
      file: '',
      code: '',
      display: ''
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
          language: this.language,
          tle: this.tle,
          mle: this.mle,
          consoleInput: this.stdin,
          code: encodeURIComponent(this.code),
          password: 'aikxNo.1'
        }
      }).then((response) => {
        console.log(response.data)
        this.display = response.data.result || response.data.error
      }).catch((err) => {
        console.log(err)
        if (err.statusCode === 408) {
          this.display = '运行超时'
        } else if (err.statusCode === 500) {
          this.display = '服务器内部错误'
        } else {
          this.display = '错误：' + err
        }
      })
    },
    tab (e) {
      const textarea = document.getElementById('code')
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const text = textarea.value
      const before = this.code.substring(0, start)
      const after = this.code.substring(end, text.length)
      textarea.value = before + '    ' + after
      textarea.selectionStart = textarea.selectionEnd = start + 4
      textarea.focus()
      e.preventDefault()
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

  input + label {
    pointer-events: none;
  }
</style>
