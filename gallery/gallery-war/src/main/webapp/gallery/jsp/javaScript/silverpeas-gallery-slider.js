/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
(function($) {

  $.gallerySlider = {
    webServiceContext : webContext + '/services',
    initialized : false,
    doInitialize : function() {
      if (!$.gallerySlider.initialized) {
        $.gallerySlider.initialized = true;
        $.i18n.properties({
          name : 'galleryBundle',
          path : webContext + '/services/bundles/org/silverpeas/gallery/multilang/',
          language : '$$', /* by default the language of the user in the current session */
          mode : 'map'
        });
      }
    }
  }

  /**
   * The different gallerySlider methods handled by the plugin.
   */
  var methods = {

    /**
     * Album by default.
     */
    init : function(options) {
      album(options);
    },

    /**
     * Handles the slider for an album. It accepts one parameter that is an
     * object with two mandatory attributes at least and some other parameters :
     * - componentInstanceId : the id of the current component instance (mandatory),
     * - albumId : the id of the aimed album (mandatory)
     * - fromPhotoId : the id of the photo from which the slider has to start,
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
        alert("Bad component instance id or album id");
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
      alert("Silverpeas GallerySlider JQuery Plugin is required.");
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
      waitInSeconds : 5,
      fromPhotoId : null,
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
          alert(errorThrown);
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
      trueFullscreen : false,
      popupLinks : false,
      keepSource : false,
      width : options.width,
      height : options.height,
      idleMode : options.idleMode,
      dummy : options.dummyImage
    };
    var data = __toGalleriaData(options, album, sliderOptions);
    $.extend(sliderOptions, {dataSource : data, show : sliderOptions.startSlide});

    // The first start of the slider
    var $playPauseButton = __buildButton($base, $this, 'playPause');
    var $stopButton = __buildButton($base, $this, 'stop');
    $base.append($playPauseButton);
    $base.append($stopButton);

    // Start Slider
    Galleria.run($this, sliderOptions);
    Galleria.ready(function() {
      var $slider = this;
      __configureSlider($base, $slider, options);

      // Popup
      var settings = {
        title : $.i18n.prop('gallery.diaporama'),
        width : options.width,
        height : options.height,
        callbackOnClose : function() {
          __onDialogClose($(this), $slider);
        }
      };

      // Buttons
      $base.popup('basic', settings);
      __configureButtonPosition('playPause', $base, $playPauseButton, options);
      __configureButtonPosition('stop', $base, $stopButton, options);
      $.popup.hideWaiting();
    });
  }

  /**
   * Private function that centralizes treatments on the diaog close event.
   * @private
   */
  function __onDialogClose($dialog, $slider) {
    // Removing the DOM elements
    $slider.destroy();
    $slider.remove();
    // Cleaning caches of galleria.io plug-in
    __clearCaches();
    // Removing the dialog elements
    $dialog.dialog("destroy");
    $dialog.remove();
  }

  /**
   * Private function that centralizes the slider configuration.
   * @param $base
   * @param $slider
   * @param options
   * @private
   */
  function __configureSlider($base, $slider, options) {
    $slider.bind("play", function() {
      $base.trigger('galleryTogglePlay');
      if (options.callbackPlay) {
        options.callbackPlay();
      }
    });
    $slider.bind("pause", function() {
      $base.trigger('galleryTogglePlay');
      if (options.callbackPause) {
        options.callbackPause();
      }
    });
    $slider.bind("fullscreen_enter", function() {
      if (options.callbackEnterFullScreen) {
        options.callbackEnterFullScreen();
      }
    });
    $slider.bind("fullscreen_exit", function() {
      $base.trigger('_fromFullScreen', [$slider]);
      if (options.callbackExitFullScreen) {
        options.callbackExitFullScreen();
      }
    });

    // Keymap
    $slider.attachKeyboard({
      37 : $slider.prev, // left
      39 : $slider.next, // right
      13 : function() {
        // toggle fullscreen when return (keyCode 13) is pressed:
        __toggleFullscreen($base, this);
      },
      32 : function() {
        // toggle playing when space (keyCode 32) is pressed:
        __togglePlay($slider);
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
    // Creating
    var $base = $("<div>").css('display', 'block').css('border', '0px').css('padding',
            '0px').css('margin', '0px auto').css('text-align', 'center').css('background-color',
            'white');
    $(document.body).append($base.append($sliderContainer));

    // Fullscreen handling
    $base.on('_toFullScreen', function(e, $slider) {
      $base.dialog("option", "closeOnEscape", false);
      // Entering fullscreen if not yet done
      if (!$slider.isFullscreen()) {
        $slider.enterFullscreen();
      }
    });
    $base.on('_fromFullScreen', function(e, $slider) {
      // Exiting fullscreen if not yet done
      if ($slider.isFullscreen()) {
        $slider.exitFullscreen();
      }
      $base.dialog("option", "closeOnEscape", true);
    });
    return $base;
  }

  /**
   * Private function that centralizes slider play handling.
   * @param $slider
   * @private
   */
  function __togglePlay($slider) {
    $slider.playToggle();
  }

  /**
   * Private function that centralizes fullscreen handling.
   * @param $base
   * @param $slider
   * @private
   */
  function __toggleFullscreen($base, $slider) {
    if (!$slider.isFullscreen()) {
      $base.trigger('_toFullScreen', [$slider]);
    } else {
      $slider.trigger(jQuery.Event("keydown", { keyCode : 27 }), [$slider]);
    }
  }

  /**
   * Private function that centralizes the slider data creation.
   */
  function __toGalleriaData(options, album, sliderOptions) {
    var data = [];
    if (album.photos) {
      sliderOptions.startSlide = 0;
      var photoIndex = 0;
      for (var photoId in album.photos) {
        var photo = album.photos[photoId];

        data.push({
          image : photo.previewUrl,
          thumb : photo.previewUrl,
          big : photo.url,
          title : photo.title,
          description : photo.description,
          link : (options.callbackLink) ? options.callbackLink(photo) : null
        });

        // If slider has to start at a specific media
        if (photo.id == options.fromPhotoId) {
          sliderOptions.startSlide = photoIndex;
        }
        photoIndex++;
      }
    }
    return data;
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
        __togglePlay($this.data('galleria'));
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
          $.i18n.prop(buttonHelpPrefix) + '\n' + $.i18n.prop('gallery.run.play.help'));
    } else {
      iconFileName = 'fullscreen.png';
      $buttonContainer.attr('title', $.i18n.prop('gallery.display.fullscreen') + '\n' +
          $.i18n.prop('gallery.display.fullscreen.help'));
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

  /**
   * Clears the caches of galleria.io plug-in.
   * @private
   */
  function __clearCaches() {

    /*
     * Current version of galleria.io plugin has problems with its destroy feature and the
     * technic here is to reload plugin to perform a destroy
     */

    $.ajax({
      url : webContext + "/gallery/jsp/javaScript/slider/galleria-1.2.9.min.js",
      async : false,
      dataType : "script"
    });
    $.ajax({
      url : webContext + "/gallery/jsp/styleSheets/slider/themes/classic/galleria.classic.min.js",
      async : false,
      dataType : "script"
    });
  }
})(jQuery);

/*
 HELPERS
 */

/*
 * Display the slider of gallery component
 */
function displayAlbumGallerySlider(options) {
  $.popup.showWaiting();
  $("<div>").appendTo(document.body).gallerySlider('album', options);
}