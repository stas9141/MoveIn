package com.example.movein.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object ApartmentDetails : Screen("apartment_details")
    object Dashboard : Screen("dashboard")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object Settings : Screen("settings")
    object DefectList : Screen("defect_list")
    object AddEditDefect : Screen("add_edit_defect")
    object DefectDetail : Screen("defect_detail/{defectId}") {
        fun createRoute(defectId: String) = "defect_detail/$defectId"
    }
}
