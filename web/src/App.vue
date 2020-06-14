<template>
  <div id="app" class="container">
    <h4>在线IDE</h4>
    <div class="row">
      <div class="input-field col s4">
        <select v-model="language">
          <option value="python3" selected>Python3(3.8.3)</option>
          <option value="g++">C++(g++8.3.0)</option>
          <option value="gcc">C(gcc8.3.0)</option>
          <option value="java11">Java(jdk11)</option>
          <option value="node">Javascript(Node10)</option>
        </select>
        <label>选择语言与编译器</label>
      </div>
      <div class="input-field col s5 offset-s1" id="tle">
        <input class="validate" type="number" v-model="tle" min="0" max="30">
        <label for="tle">时间限制(s)</label>
      </div>
      <div class="input-field btn col s1 offset-s1" @click="run">运行</div>
      <div class="input-field col s8">
        <input type="text" id="stdin" v-model="stdin">
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
            <textarea rows="20" id="code" v-model="code" @keydown.tab="tab($event)"></textarea>
          </div>
        </div>
      </div>
      <div class="col s5">
        <div class="card">
          <div class="card-content">
            <textarea rows="20" readonly="readonly" v-model="display"></textarea>
          </div>
        </div>
      </div>
    </div>
    <div id="judge" class="modal">
      <div class="modal-content">
        <h4>{{judge}}!</h4>
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
      language: 'python3',
      tle: '',
      stdin: '',
      code: '',
      display: '',
      judge: ''
    }
  },
  mounted () {
    M.AutoInit()
  },
  methods: {
    run () {
      this.display = '代码运行中...'
      const files = document.querySelector('input[type=file]').files
      const form = new FormData()
      if (files.length === 1) {
        form.append('file', files[0])
      }
      form.append('language', this.language)
      form.append('tle', this.tle)
      form.append('stdin', encodeURIComponent(this.stdin))
      form.append('code', encodeURIComponent(this.code))
      form.append('password', 'aikxNo.1')
      axios.post('/api/run', form, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }).then((response) => {
        console.log(response.data)
        this.display = response.data.result || response.data.error
        this.judge = response.data.judge
        if (this.judge) {
          M.Modal.getInstance(document.querySelector('#judge')).open()
        }
      }).catch((err) => {
        console.log(err)
        this.display = '未知错误'
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
    margin-top: 30px;
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
