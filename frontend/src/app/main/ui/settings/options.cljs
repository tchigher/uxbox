;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2020 UXBOX Labs SL

(ns app.main.ui.settings.options
  (:require
   [rumext.alpha :as mf]
   [cljs.spec.alpha :as s]
   [app.main.ui.icons :as i]
   [app.main.data.users :as udu]
   [app.main.data.messages :as dm]
   [app.main.ui.components.forms :refer [select submit-button form]]
   [app.main.refs :as refs]
   [app.main.store :as st]
   [app.util.dom :as dom]
   [app.util.forms :as fm]
   [app.util.i18n :as i18n :refer [t tr]]))

(s/def ::lang (s/nilable ::fm/not-empty-string))
(s/def ::theme (s/nilable ::fm/not-empty-string))

(s/def ::options-form
  (s/keys :opt-un [::lang ::theme]))

(defn- on-error
  [form error])

(defn- on-submit
  [form event]
  (dom/prevent-default event)
  (let [data (:clean-data form)
        on-success #(st/emit! (dm/success (tr "settings.notifications.profile-saved")))
        on-error #(on-error % form)]
    (st/emit! (udu/update-profile (with-meta data
                                    {:on-success on-success
                                     :on-error on-error})))))

(mf/defc options-form
  [{:keys [locale profile] :as props}]
  [:& form {:class "options-form"
            :on-submit on-submit
            :spec ::options-form
            :initial profile}

   [:h2 (t locale "settings.language-change-title")]

   [:& select {:options [{:label "English" :value "en"}
                         {:label "Français" :value "fr"}
                         {:label "Español" :value "es"}
                         {:label "Русский" :value "ru"}]
               :label (t locale "settings.language-label")
               :default "en"
               :name :lang}]

   [:h2 (t locale "settings.theme-change-title")]
   [:& select {:label (t locale "settings.theme-label")
               :name :theme
               :default "default"
               :options [{:label "Default" :value "default"}]}]

   [:& submit-button
    {:label (t locale "settings.profile-submit-label")}]])

;; --- Password Page

(mf/defc options-page
  [props]
  (let [locale (mf/deref i18n/locale)
        profile (mf/deref refs/profile)]
    [:section.settings-options.generic-form
     [:div.forms-container
      [:& options-form {:locale locale :profile profile}]]]))
