;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2020 UXBOX Labs SL

(ns app.main.ui.dashboard.recent-files
  (:require
   [okulary.core :as l]
   [rumext.alpha :as mf]
   [app.common.exceptions :as ex]
   [app.main.constants :as c]
   [app.main.data.dashboard :as dsh]
   [app.main.refs :as refs]
   [app.main.store :as st]
   [app.main.ui.confirm :refer [confirm-dialog]]
   [app.main.ui.dashboard.grid :refer [grid]]
   [app.main.ui.icons :as i]
   [app.main.ui.keyboard :as kbd]
   [app.main.ui.modal :as modal]
   [app.util.dom :as dom]
   [app.util.i18n :as i18n :refer [t tr]]
   [app.util.router :as rt]
   [app.util.time :as dt]))

;; --- Component: Content

(def projects-ref
  (l/derived :projects st/state))

(def recent-file-ids-ref
  (l/derived :recent-file-ids st/state))

(def files-ref
  (l/derived :files st/state))

;; --- Component: Recent files

(mf/defc recent-files-header
  [{:keys [profile] :as props}]
  (let [locale (i18n/use-locale)]
    [:header#main-bar.main-bar
     [:h1.dashboard-title "Recent"]
     [:a.btn-secondary.btn-small {:on-click #(st/emit! dsh/create-project)}
      (t locale "dashboard.header.new-project")]]))

(mf/defc recent-project
  [{:keys [project files first? locale] :as props}]
  (let [project-id (:id project)
        team-id (:team-id project)
        file-count (or (:file-count project) 0)]
    [:div.recent-files-row
     {:class-name (when first? "first")}
     [:div.recent-files-row-title
      [:h2.recent-files-row-title-name {:on-click #(st/emit! (rt/nav :dashboard-project {:team-id team-id
                                                                                         :project-id project-id}))
                                        :style {:cursor "pointer"}} (:name project)]
      [:span.recent-files-row-title-info (str file-count " files")]
      (when (> file-count 0)
        (let [time (-> (:modified-at project)
                       (dt/timeago {:locale locale}))]
          [:span.recent-files-row-title-info (str ", " time)]))]
     [:& grid {:id (:id project)
               :files files
               :hide-new? true}]]))


(mf/defc recent-files-page
  [{:keys [team-id] :as props}]
  (let [projects (->> (mf/deref projects-ref)
                      (vals)
                      (sort-by :modified-at)
                      (reverse))
        files (mf/deref files-ref)
        recent-file-ids (mf/deref recent-file-ids-ref)
        locale (i18n/use-locale)
        setup  #(st/emit! (dsh/initialize-recent team-id))]

    (-> (mf/deps team-id)
        (mf/use-effect #(st/emit! (dsh/initialize-recent team-id))))

    (when (and projects recent-file-ids)
      [:*
       [:& recent-files-header]
       [:section.recent-files-page
        (for [project projects]
          [:& recent-project {:project project
                              :locale locale
                              :key (:id project)
                              :files (->> (get recent-file-ids (:id project))
                                          (map #(get files %))
                                          (filter identity)) ;; avoid failure if a "project only" files list is in global state
                              :first? (= project (first projects))}])]])))

