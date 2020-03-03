package datamodel;

import java.sql.*;
import java.util.ArrayList;

/**
 * Database is a class that specifies the interface to the movie database. Uses
 * JDBC and the MySQL Connector/J driver.
 */
public class Database {
	/**
	 * The database connection.
	 */
	private Connection conn;

	/**
	 * Create the database interface object. Connection to the database is performed
	 * later.
	 */
	public Database() { //user
		conn = null;
	}

	/* Change this method to fit your choice of DBMS --- */
	/**
	 * Open a connection to the database, using the specified user name and
	 * password.
	 *
	 * @param userName The user name.
	 * @param password The user's password.
	 * @return true if the connection succeeded, false if the supplied user name and
	 *         password were not recognized. Returns false also if the JDBC driver
	 *         isn't found.
	 */
	public boolean openConnection(String userName, String password) {
		try {
			// Connection strings for included DBMS clients:
			// [MySQL] jdbc:mysql://[host]/[database]
			// [PostgreSQL] jdbc:postgresql://[host]/[database]
			// [SQLite] jdbc:sqlite://[filepath]

			// Use "jdbc:mysql://puccini.cs.lth.se/" + userName if you using our shared
			// server
			// If outside, this statement will hang until timeout.
			conn = DriverManager.getConnection("jdbc:sqlite://C:/Users/André/Desktop/Programmering/Databas/Lab3.db",
					userName, password);

		} catch (SQLException e) {
			System.err.println(e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Close the connection to the database.
	 */
	public void closeConnection() {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		conn = null;

		System.err.println("Database connection closed.");
	}

	/**
	 * Check if the connection to the database has been established
	 *
	 * @return true if the connection has been established
	 */
	public boolean isConnected() {
		return conn != null;
	}

	public Show getShowData(String mTitle, String mDate) {
		Integer mFreeSeats = null;
		String mVenue = null;

		/* add code for database query --- */

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT theaterName, emptySeats\n" + "FROM MovieShowing\n" + "WHERE movieName = \"" + mTitle
					+ "\" AND dateOf = \"" + mDate + "\"\n" + "ORDER BY dateOf";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				mVenue = rs.getString("theaterName");
				mFreeSeats = rs.getInt("emptySeats");
			}

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return new Show(mTitle, mDate, mVenue, mFreeSeats);
	}

	private int getNextReservationnbr() {

		int resNbr = 0;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT reservationNumber\n" + "FROM Ticket\n" + "ORDER BY reservationNumber DESC";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			try {
				resNbr = rs.getInt("reservationNumber") + 1;
			} catch (SQLException e) {
				resNbr = 0;
			}

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return resNbr;
	}

	public Reservation makeReservation(Show showing) {

		int reservationNbr = getNextReservationnbr();
		int showingID = getShowingID(showing.getTitle(), showing.getDate());
		String currentUser = CurrentUser.instance().getCurrentUserId();

		PreparedStatement ps = null;

		try {
			String sql = "insert into Ticket(reservationNumber, username, showingID, movieName, theaterName) "
					+ "values (?, ?, ?, ? ,?)";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, reservationNbr);
			ps.setString(2, currentUser);
			ps.setInt(3, showingID);
			ps.setString(4, showing.getTitle());
			ps.setString(5, showing.getVenue());

			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		Reservation reserv = new Reservation(reservationNbr, showing.getTitle(), showing.getDate(), showing.getVenue());
		reserveSeat(showing);
		return reserv;
	}

	private void reserveSeat(Show showing) {

		try {
			Statement stmt = conn.createStatement();
			String sql = "UPDATE MovieShowing\n" + "SET emptySeats = emptySeats - 1\n" + "WHERE movieName = \""
					+ showing.getTitle() + "\" AND dateOf = \"" + showing.getDate() + "\"";
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean getUser(String userName) {

		String username = null;
		/* add code for database query --- */

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT username\n" + "FROM User\n" + "WHERE username = \"" + userName + "\"";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				username = rs.getString("username");
			}

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return username.equals(userName);
	}

	public ArrayList<String> getMovies() {

		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<String> list = new ArrayList<String>();

		try {
			String sql = "SELECT * \n" + "FROM Movie\n" + "ORDER BY movieName";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				String movieName = rs.getString("movieName");
				list.add(movieName);
			}

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return list;

	}

	public ArrayList<String> getShowingDate(String mName) {

		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<String> list = new ArrayList<String>();

		try {
			String sql = "SELECT dateOf\n" + "FROM MovieShowing\n" + "WHERE movieName = \"" + mName + "\"\n"
					+ "ORDER BY dateOf";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				String dateOf = rs.getString("dateOf");
				list.add(dateOf);
			}

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return list;

	}
	
	public ArrayList<Reservation> getBookings() {

		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Reservation> list = new ArrayList<Reservation>();

		try {
			String sql = "SELECT reservationNumber, Ticket.movieName, dateOf, Ticket.theaterName\n" + 
					"FROM Ticket, MovieShowing\n" + 
					"WHERE Ticket.movieName = MovieShowing.movieName AND Ticket.showingID = MovieShowing.showingID";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				int reservationNumber = rs.getInt("reservationNumber");
				String mName = rs.getString("movieName");
				String dateOf = rs.getString("dateOf");
				String theaterName = rs.getString("theaterName");
				list.add(new Reservation(reservationNumber, mName, dateOf, theaterName));
			}

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return list;

	}

	public Integer getShowingID(String mTitle, String mDate) {

		Integer showingID = 0;

		/* add code for database query --- */

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT showingID\n" + "FROM MovieShowing\n" + "WHERE movieName = \"" + mTitle
					+ "\" AND dateOf = \"" + mDate + "\"";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			showingID = rs.getInt("showingID");

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (ps != null) {
				try {
					ps.close();
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return showingID;
	}
}