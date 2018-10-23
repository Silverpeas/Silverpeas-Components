/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function($, initialValue) {
  var context = {
    $slider : initialValue
  };

  // Fullscreen is a little bit difficult to handle with videos & sounds ...
  var isFullscreen = false;

  $.gallerySlider = {
    webServiceContext : webContext + '/services',
    mediaType : {
      photo : 'Photo',
      video : 'Video',
      sound : 'Sound',
      streaming : 'Streaming'
    },
    initialized : false,
    doInitialize : function() {
      if (!$.gallerySlider.initialized) {
        $.gallerySlider.initialized = true;
        window.i18n.properties({
          name : 'galleryBundle',
          path : webContext + '/services/bundles/org/silverpeas/gallery/multilang/',
          language : '$$', /* by default the language of the user in the current session */
          mode : 'map'
        });
      }
    }
  };

  /**
   * The different gallerySlider methods handled by the plugin.
   */
  var methods = {

    /**
     * Album by default.
     */
    init : function(options) {
      methods.album.call(this, options);
    },

    /**
     * Handles the slider for an album. It accepts one parameter that is an
     * object with two mandatory attributes at least and some other parameters :
     * - componentInstanceId : the id of the current component instance (mandatory),
     * - albumId : the id of the aimed album (mandatory)
     * - fromMediaId : the id of the media from which the slider has to start,
     * - waitInSeconds : delay in seconds before sliding (5 seconds by defaults),
     * - width : width of the slider (90% of the window width by default),
     * - height : height of the slider (90 % of the window by default),
     * - callbackPlay : called after play,
     * - callbackPause : called after pause,
     * - callbackEnterFullScreen : called after entering fullscreen,
     * - callbackExitFullScreen : called after exiting fullscreen,
     * - callbackLink : called during to JSon data transforming,
     * - dummyImage : image displayed instead of an unexisting image
     */
    album : function(options) {

      // Light checking
      if (!options.componentInstanceId || !options.albumId) {
        notyWarning("Bad component instance id or album id");
        return false;
      }

      // Dialog
      return __gallerySlider($(this), options);
    }
  };

  /**
   * The gallerySlider Silverpeas plugin based on JQuery. This JQuery plugin
   * abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   *
   * Here the gallerySlider namespace in JQuery.
   */
  $.fn.gallerySlider = function(method) {

    if (!$().popup) {
      notyWarning("Silverpeas GallerySlider JQuery Plugin is required.");
      return false;
    }

    $.gallerySlider.doInitialize();
    if (methods[method]) {
      return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.init.apply(this, arguments);
    } else {
      $.error('Method ' + method + ' does not exist on jQuery.gallerySlider');
    }
  };

  /**
   * Private function that handles the gallery slider loading. Be careful,
   * options have to be well initialized before this function call
   */
  function __gallerySlider($this, options) {

    if (!$this.length) {
      return $this;
    }

    // Waiting animation
    $.popup.showWaiting();

    // Default options
    options = $.extend({
      $elementsToHide : initialValue,
      waitInSeconds : 5,
      fromMediaId : null,
      width : $(window).width() * 0.9,
      height : $(window).height() * 0.9,
      idleMode : false,
      callbackPlay : null,
      callbackPause : null,
      callbackEnterFullScreen : null,
      callbackExitFullScreen : null,
      callbackLink : null,
      dummyImage : null
    }, options);

    return $this.each(function() {
      var $this = $(this);

      // Getting gallery album data
      var url = $.gallerySlider.webServiceContext;
      url += "/gallery/" + options.componentInstanceId + "/albums/" + options.albumId;
      if (options.mediaSort) {
        url += "?sort=" + options.mediaSort;
      }
      $.ajax({
        url : url,
        type : 'GET',
        dataType : 'json',
        cache : false,
        success : function(data, status, jqXHR) {
          __startGallerySlider($this, options, data);
        },
        error : function(jqXHR, textStatus, errorThrown) {
          $.popup.hideWaiting();
          notyError(errorThrown);
        }
      });
    })
  }

  /**
   * Private function that centralizes the slider starting
   */
  function __startGallerySlider($this, options, album) {

    // Initializing dialog
    var $base = __buildDialogContainer($this);

    // Slider Options
    var sliderOptions = {
      debug : false,
      autoplay : (options.waitInSeconds * 1000),
      lightbox : false,
      imageCrop : false,
      trueFullscreen : true,
      popupLinks : false,
      keepSource : false,
      width : options.width,
      height : options.height,
      idleMode : options.idleMode,
      dummy : options.dummyImage
    };
    var data = __toGalleriaData(options, album, sliderOptions);
    $.extend(sliderOptions, {dataSource : data, show : sliderOptions.startSlide});

    var firstDisplay = (!$this.data('galleria'));
    if (firstDisplay) {
      // The first start of the slider
      var $playPauseButton = __buildButton($base, $this, 'playPause');
      var $stopButton = __buildButton($base, $this, 'stop');
      $base.append($playPauseButton);
      $base.append($stopButton);

      // Start Slider
      Galleria.run($this, sliderOptions);
      Galleria.ready(function() {
        context.$slider = this;
        if (firstDisplay) {
          firstDisplay = false;
          __configureSlider($base, sliderOptions, options);

          // Popup
          var settings = {
            title : window.i18n.prop('gallery.diaporama'),
            width : options.width,
            height : options.height,
            callbackOnClose : function() {
              if (options.$elementsToHide && options.$elementsToHide.length > 0) {
                options.$elementsToHide.show();
              }
              __onDialogClose();
            }
          };

          // Buttons
          if (options.$elementsToHide && options.$elementsToHide.length > 0) {
            options.$elementsToHide.hide();
          }
          $base.popup('basic', settings);
          __configureButtonPosition('playPause', $base, $playPauseButton, options);
          __configureButtonPosition('stop', $base, $stopButton, options);
          $.popup.hideWaiting($base);
        }
      });
    } else {
      if (options.$elementsToHide && options.$elementsToHide.length > 0) {
        options.$elementsToHide.hide();
      }
      context.$slider = $this.data('galleria');
      $.popup.hideWaiting();
      $base.dialog("open");
      context.$slider.load(data);
      __configureSlider($base, sliderOptions, options);
      context.$slider.show(sliderOptions.startSlide);
    }
  }

  /**
   * Private function that centralizes treatments on the diaog close event.
   * @private
   */
  function __onDialogClose() {
    $('#slideshow').find('.embed').remove();
    context.$slider.unbind("play");
    context.$slider.unbind("pause");
    context.$slider.unbind("fullscreen_enter");
    context.$slider.unbind("fullscreen_exit");
    context.$slider.detachKeyboard();
    context.$slider.pause();
  }

  /**
   * Private function that centralizes the slider configuration.
   * @param $base
   * @param sliderOptions
   * @param options
   * @private
   */
  function __configureSlider($base, sliderOptions, options) {
    context.$slider.bind("thumbnail", function(event) {
      setTimeout(function() {
        var $thumbContainer = $(event.thumbTarget).parent();
        var currentMedia = sliderOptions.dataSource[event.index];
        var text = currentMedia.title;
        if (currentMedia.author) {
          text += ' / ' + currentMedia.author;
        }
        $thumbContainer.qtip({
          'style' : {
            'classes' : 'qtip-youtube'
          },
          'content' : {
            'text' : "<span>" + text + "</span>"
          },
          'position' : {
            'adjust' : {
              'method' : "flip flip"
            },
            'viewport' : $(window)
          }
        });
      }, 100);
    });
    context.$slider.bind("loadfinish", function(event) {
      context.$currentImageTarget = $(event.imageTarget);
      context.$currentMediaContainer = context.$currentImageTarget.parent();
      context.currentMedia = sliderOptions.dataSource[event.index];
      if (context.currentMedia.type === $.gallerySlider.mediaType.video ||
          context.currentMedia.type === $.gallerySlider.mediaType.sound) {
        var $playerStartButton = $('.galleria-videoicon', context.$currentMediaContainer);
        $playerStartButton.unbind("click");
        $playerStartButton.bind("click", function() {
          __buildEmbedPlayer();
          context.$slider.enterMediaPlayerFullscreen = function() {
            __buildEmbedPlayer();
          };
          context.$slider.exitMediaPlayerFullscreen = function() {
            __buildEmbedPlayer();
          };
        });
      } else {
        context.$slider.enterMediaPlayerFullscreen = initialValue;
        context.$slider.exitMediaPlayerFullscreen = initialValue;
      }
    });
    context.$slider.bind("play", function() {
      $base.trigger('galleryTogglePlay');
      if (options.callbackPlay) {
        options.callbackPlay();
      }
    });
    context.$slider.bind("pause", function() {
      $base.trigger('galleryTogglePlay');
      if (options.callbackPause) {
        options.callbackPause();
      }
    });
    context.$slider.bind("fullscreen_enter", function() {
      if (typeof context.$slider.enterMediaPlayerFullscreen === 'function') {
        setTimeout(function() {
          context.$slider.enterMediaPlayerFullscreen();
        }, 500);
      }
      if (options.callbackEnterFullScreen) {
        options.callbackEnterFullScreen();
      }
    });
    context.$slider.bind("fullscreen_exit", function() {
      $base.trigger('_fromFullScreen', [context.$slider]);
      if (typeof context.$slider.exitMediaPlayerFullscreen === 'function') {
        setTimeout(function() {
          context.$slider.exitMediaPlayerFullscreen();
        }, 500);
      }
      if (options.callbackExitFullScreen) {
        options.callbackExitFullScreen();
      }
    });

    // Keymap
    context.$slider.attachKeyboard({
      37 : context.$slider.prev, // left
      39 : context.$slider.next, // right
      13 : function() {
        // toggle fullscreen when return (keyCode 13) is pressed:
        __toggleFullscreen($base);
      },
      32 : function() {
        // toggle playing when space (keyCode 32) is pressed:
        __togglePlay();
      },
      73 : function() {
        // toggle info when 'i' key (keyCode 73) is pressed:
        this.$('info-link,info-close,info-text').toggle();
      }
    });
  }

  /**
   * Private function that centralizes the build of dialog container.
   * @param $sliderContainer
   * @return {*|HTMLElement}
   * @private
   */
  function __buildDialogContainer($sliderContainer) {
    var $base = $("#slideshow");
    if ($base.length == 0) {
      var $fullscreenSwitcher;
      $base = $("<div>").attr('id', 'slideshow').css('display', 'block').css('border',
          '0px').css('padding', '0px').css('margin', '0px auto').css('text-align',
          'center').css('background-color', 'white');
      $fullscreenSwitcher = $("<div>").attr('id', 'slideshow_fullscreenSwitcher').css('display',
          'block').css('border', '0px').css('padding', '0px').css('margin',
          '0px auto').css('text-align', 'center').css('background-color', 'white');
      $(document.body).append($base);
      $fullscreenSwitcher.append($sliderContainer);
      $base.append($fullscreenSwitcher);

      // Fullscreen handling
      $base.on('_toFullScreen', function(e) {
        $base.dialog("option", "closeOnEscape", false);
        // Entering fullscreen if not yet done
        if (!context.$slider.isFullscreen()) {
          context.$slider.enterFullscreen();
        }
      });
      $base.on('_fromFullScreen', function(e) {
        // Exiting fullscreen if not yet done
        if (context.$slider.isFullscreen()) {
          context.$slider.exitFullscreen();
        }
        $base.dialog("option", "closeOnEscape", true);
      });
    }
    return $base;
  }

  /**
   * Private function that centralizes slider play handling.
   * @private
   */
  function __togglePlay() {
    context.$slider.playToggle();
  }

  /**
   * Private function that centralizes fullscreen handling.
   * @param $base
   * @private
   */
  function __toggleFullscreen($base) {
    if (!context.$slider.isFullscreen()) {
      $base.trigger('_toFullScreen', [context.$slider]);
    } else {
      context.$slider.trigger(jQuery.Event("keydown", { keyCode : 27 }), [context.$slider]);
    }
  }

  /**
   * Private function that centralizes the slider data creation.
   */
  function __toGalleriaData(options, album, sliderOptions) {
    var data = [];
    if (album.mediaList) {
      sliderOptions.startSlide = 0;
      var mediaIndex = 0;
      $.each(album.mediaList, function(index, media) {
        var currentMediaData = {
          type : media.type
        };
        switch (media.type) {
          case $.gallerySlider.mediaType.photo :
            currentMediaData.image = media.previewUrl;
            currentMediaData.big = media.normalUrl;
            currentMediaData.link = (options.callbackLink) ? options.callbackLink(media) : null;
            break;
          case $.gallerySlider.mediaType.sound :
          case $.gallerySlider.mediaType.video :
            currentMediaData.video = media.embedUrl;
            currentMediaData.image = media.thumbUrl;
            currentMediaData.big = media.thumbUrl;
            break;
          case $.gallerySlider.mediaType.streaming :
            if(media.provider === 'vimeo') {
              currentMediaData.video = media.url;
              currentMediaData.thumb = null;
            } else {
              currentMediaData = null;
            }
            break;
          default:
            currentMediaData = null;
            break;
        }

        // If media is not handled, then it is ignored.
        if (currentMediaData == null) {
          return;
        }

        if (typeof currentMediaData.thumb === 'undefined') {
          currentMediaData.thumb = media.thumbUrl;
        }
        currentMediaData.title = media.title;
        currentMediaData.description = media.description;
        currentMediaData.author = media.author;
        data.push(currentMediaData);

        // If slider has to start at a specific media
        if (media.id == options.fromMediaId) {
          sliderOptions.startSlide = mediaIndex;
        }
        mediaIndex++;
      });
    }
    return data;
  }

  /**
   * Private function that builds the embed player and returns the parent container.
   * @private
   */
  function __buildEmbedPlayer() {
    if (context.$slider.isPlaying()) {
      context.$slider.pause();
    }
    var $playerFrame = $('.galleria-frame', context.$currentMediaContainer);
    $playerFrame.embedPlayer({
      url : context.currentMedia.video,
      width : context.$currentMediaContainer.parent().width() - 120,
      height : context.$currentMediaContainer.parent().height(),
      playerParameters : {
        autoPlay : true,
        backgroundColor : 'transparent'
      }
    });

    $playerFrame.css('margin-left', '60px');
    $playerFrame.show();

    return $playerFrame;
  }

  /**
   * Private function that centralizes a button construction
   */
  function __buildButton($base, $this, type) {

    // Initializing
    var $buttonContainer = $('<div>').addClass('gallery-slider-player-buttons').css('position',
        'absolute').css('top', '0px').css('left', '0px').css('display', 'none');

    // This second call permits to load required images for a simple button
    var $button = __configureVisualButtonAspect(type, $buttonContainer, false, $this);
    $buttonContainer.append($button);

    // Setting onclick result
    if (type == 'playPause') {
      $base.on('galleryTogglePlay', function() {
        __configureVisualButtonAspect(type, $buttonContainer, $button, $this);
      });
    }
    $buttonContainer.click(function() {
      if (type == 'playPause') {
        __togglePlay();
        $base.trigger('galleryTogglePlay');
      } else {
        $base.trigger('_toFullScreen', [$this.data('galleria')]);
      }
    });
    return $buttonContainer;
  }

  /**
   * Private function that centralizes the configuration of a button on visual side
   */
  function __configureVisualButtonAspect(type, $buttonContainer, $button, $this) {

    // Initializing the image if necessary
    if (!$button) {
      $button = $('<img>').css('width', '20px').css('height', '20px');
    }

    // Choosing the right image
    var iconFileName;
    if (type == 'playPause') {
      var buttonHelpPrefix;
      if (!$this.data('galleria') || $this.data('galleria').isPlaying()) {
        iconFileName = 'pause.png';
        buttonHelpPrefix = 'gallery.run.standby';
      } else {
        iconFileName = 'play.png';
        buttonHelpPrefix = 'gallery.run.play';
      }
      $buttonContainer.attr('title',
          window.i18n.prop(buttonHelpPrefix) + '\n' + window.i18n.prop('gallery.run.play.help'));
    } else {
      iconFileName = 'fullscreen.png';
      $buttonContainer.attr('title', window.i18n.prop('gallery.display.fullscreen') + '\n' +
          window.i18n.prop('gallery.display.fullscreen.help'));
    }

    // Setting the image source attribute
    $button.attr('src', webContext + '/util/icons/player/' + iconFileName);
    return $button;
  }

  /**
   * Private function that centralizes the position configuration of a button
   */
  function __configureButtonPosition(type, $target, $buttonContainer, $options) {
    if ($buttonContainer) {

      // Top
      var top = $(document).scrollTop() + 15;

      // Left
      var left = $(document).scrollLeft() - 15;
      if (type == 'playPause') {
        left += ($options.width - (20 * 2)) - 5;
      } else {
        left += ($options.width - 20);
      }

      // Changing the position
      $buttonContainer.offset({ top : top, left : left });
      $buttonContainer.show();
    }
  }
})(jQuery, undefined);