define([
	"dojo/_base/declare",
	"dijit/_WidgetBase",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"mycore/classification/_SettingsMixin",
	"dojo/text!./templates/Editor.html",
	"dojo/on", // on
	"dojo/dom", // byId
	"dojo/query",
	"dojo/_base/lang", // hitch, clone
	"dojo/dom-construct",
	"mycore/common/I18nManager",
	"dijit/Toolbar",
	"dijit/layout/ContentPane",
	"dijit/layout/BorderContainer",
	"dijit/form/Button",
	"dijit/Tooltip",
	"mycore/classification/Store",
	"mycore/classification/CategoryEditorPane",
	"mycore/classification/TreePane",
	"mycore/common/I18nStore",
	"mycore/classification/SettingsDialog"
], function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, _SettingsMixin, template, on, dom, query, lang, domConstruct, i18n) {

	/**
	 * Create a new instance of the classification editor.
	 * 
	 * @param settings json object to configure the classification editor.
	 * The following parameters are required:
	 *   webAppBaseURL: base url of web application (e.g. http://localhost:8291/)
	 *   resourceURL: url of resource (e.g. http://localhost:8291/rsc/classifications/)
	 * The following parameters are optional:
	 *   showId: are classification id and category id are editable (true | false)
	 *   supportedLanguages: which languages are available for selection (json array ["de", "en", "pl"])
	 *   language: the current language (e.g. "de")
	 *   editable: if the user can edit and dnd
	 */
return declare("mycore.classification.Editor", [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, _SettingsMixin], {
	templateString: template,
	widgetsInTemplate: true,

	baseClass: "classeditor",

	store: null,

	settingsDialog: null,

	disabled: false,

	constructor: function(/*Object*/ args) {
		declare.safeMixin(this, args);
	},

	create: function(args) {
		// i18n
		var i18nStore = new mycore.common.I18nStore({
			url: this.settings.webAppBaseURL + "servlets/MCRLocaleServlet/"
		});
		i18n.setLanguage(this.settings.language);
		i18n.init(i18nStore);
		i18n.fetch("component.classeditor");
		// settings dialog
		this.settingsDialog = new mycore.classification.SettingsDialog();
		// create loader
		this.store = new mycore.classification.Store();
		// call inherited (includes distributing settings to other classes)
		this.inherited(arguments);
		// toolbar tooltips
		i18n.resolveTooltip(this.saveButton);
		i18n.resolveTooltip(this.refreshButton);
		i18n.resolveTooltip(this.settingsButton);
		// toolbar events
		on(this.saveButton, "click", lang.hitch(this, this.onSaveClicked));
		on(this.refreshButton, "click", lang.hitch(this, this.onRefreshClicked));
		on(this.settingsButton, "click", lang.hitch(this, this.onSettingsClicked));
		on(this.fullscreenButton, "click", lang.hitch(this, this.onFullscreenClicked));
		// tree events
		on(this.treePane.tree, "itemSelected", lang.hitch(this, this.onTreeItemSelected));
		on(this.treePane.tree, "itemAdded", lang.hitch(this, this.onTreeItemAddedOrMoved));
		on(this.treePane.tree, "itemMoved", lang.hitch(this, this.onTreeItemAddedOrMoved));
		on(this.treePane.tree, "itemsRemoved", lang.hitch(this, this.onTreeItemsRemoved));
		// category editor pane events
		on(this.categoryEditorPane, "labelChanged", lang.hitch(this, this.onCategoryEditorLabelChanged));
		on(this.categoryEditorPane, "urlChanged", lang.hitch(this, this.onCategoryEditorURLChanged));
		on(this.categoryEditorPane, "idChanged", lang.hitch(this, this.onCategoryEditorIdChanged));
		// settings dialog event
		on(this.settingsDialog, "hide", lang.hitch(this, this.onSettingsDialogClose));
		on(this.settingsDialog, "saveBeforeImport", lang.hitch(this, this.onSaveBeforeImport));
		// store events
		on(this.store, "saved", lang.hitch(this, this.onStoreSaved));
		on(this.store, "saveEvent", lang.hitch(this, this.onStoreSaveEvent));
		on(this.store, "saveError", lang.hitch(this, this.onStoreSaveError));
	},

	startup: function() {
		this.inherited(arguments);
		this.borderContainer.resize();
	},

	onTreeItemSelected: function(args) {
		if(args.item == null) {
			this.categoryEditorPane.set("disabled", true);
		} else {
			this.categoryEditorPane.set("disabled", !(this.settings.editable || this.settings.editable == undefined));
			this.categoryEditorPane.update(args.item);
		}
	},

	onTreeItemAddedOrMoved: function(args) {
		this.store.updateSaveArray("update", args.item, args.parent);
		this.updateToolbar();
	},

	onTreeItemsRemoved: function(args) {
		for(var i = 0; i < args.items.length; i++) {
			this.store.updateSaveArray("delete", args.items[i]);
		}
		this.categoryEditorPane.set("disabled", true);
		this.updateToolbar();
	},

	onCategoryEditorLabelChanged: function(args) {
		this._updateItem(args.item, "labels", args.value);
	},

	onCategoryEditorURLChanged: function(args) {
		this._updateItem(args.item, "uri", args.value);
	},

	onCategoryEditorIdChanged: function(args) {
		this._updateItem(args.item, "id", args.value);
	},

	_updateItem: function(item, type, value) {
		this.store.setValue(item, type, value);
		this.store.setValue(item, "modified", true);
		this.store.updateSaveArray("update", item);
		this.updateToolbar();
	},

	onSaveClicked: function() {
		this.store.save(this.treePane.tree);
	},

	onRefreshClicked: function() {
		if(!confirm(i18n.getFromCache("component.classeditor.refresh.warning"))) {
			return;
		}
		this.reloadClassification();
	},

	onSettingsClicked: function() {
		this.openSettingsDialog();
	},

	onFullscreenClicked: function() {
		this.toggleFullscreen();
		this.fullscreenButton.set("hovering", false);
		// because fullscreen is not applied instantly
		setTimeout(lang.hitch(this, function() {
			this.updateToolbar();
		}), 300);
	},

	onSettingsDialogClose: function() {
		var languages = this.settingsDialog.languageEditor.get("value");
		i18n.setLanguages(languages);
		this.categoryEditorPane.updateLanguages();
		// show Id's?
		var showIds = this.settingsDialog.showIdCheckBox.get("value");
		if(showIds) {
			this.treePane.showId();
			this.categoryEditorPane.showId();
		} else {
			this.treePane.hideId();
			this.categoryEditorPane.hideId();
		}
		// if a new classification was successfully imported
		if(this.settingsDialog.classificationImported) {
			this.settingsDialog.classificationImported = false;
			this.reloadClassification();
		}
	},

	onSaveBeforeImport: function() {
		this.store.save(this.treePane.tree);
	},

	onStoreSaved: function() {
		this.updateToolbar();
		this.categoryEditorPane.update();
		alert(i18n.getFromCache("component.classeditor.save.successfull"));
	},

	onStoreSaveError: function(error) {
		console.log(error);
	},

	onStoreSaveEvent: function(event) {
		if(event.xhr.status == 401) {
	        alert(i18n.getFromCache("component.classeditor.save.nopermission"));
	    } else{
	        alert(i18n.getFromCache("component.classeditor.save.generalerror") + " - " + evt.error);
			console.log("error while saving");
			console.log(evt.error);
	    }
	},

	/**
	 * Loads a new classification - if this string is empty, all
	 * classifications are loaded.
	 */
	loadClassification: function(/*String*/ classificationId, /*String*/ categoryId) {
		this.store.classificationId = classificationId;
		this.store.categoryId = categoryId;
		this.reloadClassification();
	},

	/**
	 * Reloads the tree.
	 */
	reloadClassification: function() {
		this.store.load(
			lang.hitch(this, function(store) {
				this.treePane.tree.createTree(store);
				this.treePane.updateToolbar();
				this.updateToolbar();
			}),
			lang.hitch(this, function(event) {
				if(event.xhr.status == 401) {
					alert(i18n.getFromCache("component.classeditor.error.noReadPermission"));
				} else {
					alert(event.xhr.statusText);
					console.log(event);
				}
				this.set("disabled", true);
			})
		);
		this.categoryEditorPane.set("disabled", true);
	},

	updateToolbar: function() {
		var disabled = this.get("disabled") == true;
		var dirty = this.store.isDirty();
		var disabledSave = disabled || !dirty;
		this.saveButton.set("disabled", disabledSave);
		this.saveButton.set("iconClass", "icon16 " + (disabledSave ? "saveDisabledIcon" : "saveIcon"));
		this.refreshButton.set("disabled", disabled);
		this.refreshButton.set("iconClass", "icon16 " + (disabled ? "refreshDisabledIcon" : "refreshIcon"));
		this.settingsButton.set("disabled", disabled);
		this.settingsButton.set("iconClass", "icon16 " + (disabled ? "settingsDisabledIcon" : "settingsIcon"));
		this.fullscreenButton.set("disabled", disabled);
		if(this.isFullscreen()) {
			this.fullscreenButton.set("iconClass", "icon16 " + (disabled ? "minimizeDisabledIcon" : "minimizeIcon"));
		} else {
			this.fullscreenButton.set("iconClass", "icon16 " + (disabled ? "fullscreenDisabledIcon" : "fullscreenIcon"));
		}
	},

	openSettingsDialog: function() {
		this.settingsDialog.show(this.store.isDirty());
	},

	toggleFullscreen: function() {
		var fullscreen = this.isFullscreen();
		// toggle fullscreen
		if (this.domNode.requestFullScreen) {
			if (!fullscreen) {
				this.domNode.requestFullscreen();
			} else {
				document.exitFullScreen();
			}
		} else if (this.domNode.mozRequestFullScreen) {
			if (!fullscreen) {
				this.domNode.mozRequestFullScreen();
			} else {
				document.mozCancelFullScreen();
			}
		} else if (this.domNode.webkitRequestFullScreen) {
			if (!fullscreen) {
				this.domNode.webkitRequestFullScreen();
			} else {
				document.webkitCancelFullScreen();
			}
		}
		// move outer div's
		if(fullscreen) {
			domConstruct.place(this.settingsDialog.domNode, query("body")[0]);
			domConstruct.place(this.treePane.exportDialog.domNode, query("body")[0]);
		} else {
			domConstruct.place(this.settingsDialog.domNode, this.domNode);
			domConstruct.place(this.treePane.exportDialog.domNode, this.domNode);
		}
	},

	isFullscreen: function() {
		return document.fullScreen || document.mozFullScreen || document.webkitIsFullScreen;
	},

	_setDisabledAttr: function(/*boolean*/ disabled) {
		this.disabled = disabled;
		this.updateToolbar();
		this.treePane.set("disabled", disabled);
		this.categoryEditorPane.set("disabled", disabled);
	}

});
});
