
(function() {

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/survey/jsp/js/vuejs/components/silverpeas-survey-result-chart-templates.jsp');

  let count = 0;
  const newId = function() {
    return count++;
  }

  const AnswerPercentMixin = {
    props : {
      answerPercent : {
        'type' : Object,
        'required' : true
      },
      anonymous : {
        'type' : Boolean,
        'required' : true,
        'default' : true
      }
    }
  }

  const AnswerMixin = {
    props : {
      answer : {
        'type' : Object,
        'required' : true
      },
      anonymous : {
        'type' : Boolean,
        'required' : true,
        'default' : true
      }
    }
  }

  window.SurveyResultChart = {
    mountQuestionAnswerResult : function(options) {
      options = extendsObject({
            id : undefined,
            title : undefined,
            view : 'pie',
            answerPercents : undefined,
            anonymous : true
          }, options);
      whenSilverpeasReady(function() {
        const $line = document.querySelector('#' + options.id);
        const $dock = document.createElement('silverpeas-survey-question-answer-result-chart');
        $dock.setAttribute('v-bind:view', 'view');
        $dock.setAttribute('v-bind:title', 'title');
        $dock.setAttribute('v-bind:answer-percents', 'answerPercents');
        $dock.setAttribute('v-bind:anonymous', 'anonymous');
        $line.appendChild($dock);
        SpVue.createApp({
          data : function() {
            return {
              title : options.title,
              view : options.view,
              answerPercents : options.answerPercents,
              anonymous : options.anonymous
            }
          }
        }).mount($line);
      });
    }
  }

  SpVue.component('silverpeas-survey-question-answer-result-chart',
      templateRepository.get('question-answer-result-chart', {
        props : {
          title : {
            'type' : String,
            'required' : true
          },
          view : {
            'type' : String,
            'required' : true
          },
          answerPercents : {
            'type' : Array,
            'required' : true
          },
          anonymous : {
            'type' : Boolean,
            'required' : true,
            'default' : true
          }
        },
        computed : {
          pieView : function() {
            return SurveyResultChartSettings.get('c.p') === this.view;
          }
        }
      }));

  SpVue.component('survey-pie-chart',
      templateRepository.get('pie-chart', {
        mixins : [VuejsI18nTemplateMixin],
        props : {
          answerPercents : {
            'type' : Array,
            'required' : true
          },
          anonymous : {
            'type' : Boolean,
            'required' : true,
            'default' : true
          }
        },
        data : function() {
          return {
            chartDomIdContainer : 'chartDomId-' + newId(),
            chartDomId : 'chartDomId-' + newId(),
            chartApi : undefined
          };
        },
        mounted : function() {
          const options = {
            chartSelector : '#' + this.chartDomId,
            isDisplayAsBars : this.isDisplayAsBars,
            chart : this.chartData,
            formatToolTipTitle : function(label, item) {
              return item.srcData.answerPercent.answer.label;
            },
            formatToolTipValue : function(value) {
              return value + ' %';
            },
            formatTickValue : function(value) {
              return value <= 100 ? value + ' %' : '';
            },
            onItemClickHelp : function(item) {
              if (!this.anonymous && item.srcData.answerPercent.percent > 0) {
                return this.messages.seeVoterClickHelp;
              }
            }.bind(this),
            onItemClick : function(item) {
              if (options.onItemClickHelp(item)) {
                viewUsers(item.srcData.answerPercent.answer.id);
              }
            }
          }
          this.chartApi = new ChartManager(options);
        },
        methods : {
          getLegendColor : function(answerIndex) {
            const colors = this.chartApi.getColorSet();
            return colors[answerIndex % colors.length];
          }
        },
        computed : {
          chartDomId : function() {
            return 'chartDomId-' + newId();
          },
          chartDomIdContainer : function() {
            return this.chartDomId + '-container';
          },
          chartData : function() {
            return {
              chartType : 'pie',
              combine : false,
              items : this.answerPercents.map(function(answerPercent, index) {
                return {
                  answerPercent : answerPercent,
                  label : '<div id="' + this.chartDomId + '-label-' + index + '"></div>',
                  value : answerPercent.percent
                }
              }.bind(this))
            }
          },
          isDisplayAsBars : function() {
            return this.chartData.items.map(function(item) {
              return item.value;
            }).reduce(function(a, b) {
              return a + b;
            }) > 100.01;
          }
        }
      }));

  SpVue.component('survey-horizontal-bar-chart-line',
      templateRepository.get('horizontal-bar-chart-line', {
        mixins : [AnswerPercentMixin],
        methods : {
          viewUsers : function() {
            viewUsers(this.answerPercent.answer.id);
          }
        },
        computed : {
          percent : function() {
            return this.answerPercent.percent;
          },
          percentAsInt : function() {
            return Math.floor(this.answerPercent.percent);
          }
        }
      }));

  SpVue.component('survey-chart-answer-label',
      templateRepository.get('chart-answer-label', {
        mixins : [AnswerMixin],
        props : {
          color : {
            'type' : String,
            'default' : undefined
          },
          percent : {
            'type' : Number,
            'default' : undefined
          }
        },
        methods : {
          viewSuggestions : function() {
            viewSuggestions(this.answer.questionId);
          }
        }
      }));
})();
