package tn.farah.NetflixJava.Service;

import tn.farah.NetflixJava.DAO.AdminDashboardDAO;
import java.util.Map;

public class AdminDashboardService {
    private final AdminDashboardDAO dashboardDAO;

    public AdminDashboardService() {
        this.dashboardDAO = new AdminDashboardDAO();
    }

    
    public int getTotalFilms() { return dashboardDAO.getCount("film"); }
    public int getTotalSeries() { return dashboardDAO.getCount("serie"); }
    public int getTotalEpisodes() { return dashboardDAO.getCount("episode"); }
    public int getTotalUsers() { return dashboardDAO.getCount("users"); }
    public int getTotalComments() { return dashboardDAO.getCount("comment"); }

    public Map<String, Integer> getContentByYearData() {
        return dashboardDAO.getContentByYear();
    }

    public Map<String, Integer> getCommentsByTypeData() {
        return dashboardDAO.getCommentsByType();
    }
    public Map<String, Integer> getInscriptionsData() {
        
    	return dashboardDAO.getInscriptionsData(); 
    }
}