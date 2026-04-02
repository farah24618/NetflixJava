package tn.farah.NetflixJava.Service;

import tn.farah.NetflixJava.DAO.AdminDashboardDAO;
import tn.farah.NetflixJava.Entities.AdminStats;

import java.sql.Connection;

public class AdminDashboardService {

    private final AdminDashboardDAO adminDashboardDAO;

    public AdminDashboardService(Connection connection) {
        this.adminDashboardDAO = new AdminDashboardDAO(connection);
    }

    public AdminStats getDashboardStats() {
        return adminDashboardDAO.getDashboardStats();
    }
}