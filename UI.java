/* UI for allowing user to query particular MySQL DB */

// 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import com.mysql.cj.jdbc.MysqlDataSource;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.Box;

//
public class UI extends JFrame 
{
	//
	public Properties properties = new Properties();
	public FileInputStream filein = null;
	public static MysqlDataSource dataSource = null;
	private static Connection connection = null;
	
	//keep track of database connection status
	private boolean connectedToDatabase = false;
	
	//default query retrieves all data from bikes table
	static final String DEFAULT_QUERY = "SELECT * FROM riders";
	
	//
	public ResultSetTableModel tableModel;
	
	// labels for gui
	private static JLabel dbLabel = new JLabel("Enter Database Information");
	private static JLabel queryLabel = new JLabel("Enter An SQL Command");
	private static JLabel driver = new JLabel("JDBC DRIVER ");
	private static JLabel url = new JLabel("Database URL");
	private static JLabel user = new JLabel("Username ");
	private static JLabel pw = new JLabel("Password ");
	
	// text fields and text area for top portion of GUI
	private static JTextField userField = new JTextField();
	private static JPasswordField pwField = new JPasswordField();
	
	// combo boxes
	private static String[] drivers = {"", "com.mysql.cj.jdbc.Driver", "com.mysql.cj.jdbc.Driver", "com.mysql.cj.jdbc.Driver"};
	private static String[] urls = {"", "jdbc:mysql://localhost:3306/project3?useTimezone=true&serverTimezone=UTC", "jdbc:mysql://localhost:3306/project3?useTimezone=true&serverTimezone=UTC", "jdbc:mysql://localhost:3306/project3?useTimezone=true&serverTimezone=UTC"};
	private static JComboBox driverField = new JComboBox(drivers);
	private static JComboBox  urlField = new JComboBox(urls);    
	
	// text area (and scrolling pane) for top portion of GUI
	private static JTextArea queryArea = new JTextArea(DEFAULT_QUERY, 3, 100); // set up JTextArea in which user types queries
	private static JScrollPane queryPane = new JScrollPane( queryArea, 
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER 
	);
	
	// buttons for middle portion of GUI
	private static JButton status = new JButton( " No connection " );;
	private static JButton connectButton = new JButton( "Connect to Database " );
	private static JButton clearButton = new JButton( " Clear SQL Command " );
	private static JButton executeButton = new JButton( " Execute SQL Command " );
	
	// bottom of GUI
	private static JLabel resultLabel = new JLabel("SQL Execution Result Window");
	private static JButton clearResultsButton = new JButton( "Clear Result Window");
	public JTable resultTable;

	//create GUI with UI constructor
	public UI() 
	{   
		//
		super( "MySQL Client App" );
		
		// create event listener for button
	    connectButton.addActionListener(e -> {
	    	//
	    	connectedToDatabase = true;
	    	status.setText("Connected to " + (String) urlField.getSelectedItem());
	
	    });
	
	  
	    // create event listener for button
	    executeButton.addActionListener(e -> {
	        
	    	// only execute command if connected to DB
	    	if ( connectedToDatabase ) 
	    	{
	    	    // create TableModel for results of query 
	    	    try 
	    	    {     		
	    	        connection = connect();
	    	        tableModel = new ResultSetTableModel( connection );
	    	        resultTable.setModel(tableModel);
	    	    }
	    	    catch (SQLException e1) {e1.printStackTrace();} 
	    	    catch (ClassNotFoundException e1) {e1.printStackTrace();}
		        
				// parse first command in SQL query
				String[] queryList = (queryArea.getText()).split(" ");
				
		    	// check if root, admin, or regular user
		    	String usr = (String) userField.getText();
		    	if (usr.equalsIgnoreCase("admin"))
		    	{
		    		// see if query is a select query
		    		if (queryList[0].equalsIgnoreCase("select"))
		    		{
		            	// perform a new query
		                try 
		                { tableModel.setQuery( queryArea.getText() ); } 
		                catch ( SQLException sqlException ) 
		                {
		                	JOptionPane.showMessageDialog( null, 
		                           sqlException.getMessage(), 
		                           "Database error", 
		                           JOptionPane.ERROR_MESSAGE 
		                    );
		                }
		    		}
		    		
		    		// query should call update method 
		    		else 
		    		{
			            try 
			            { tableModel.setUpdate( queryArea.getText() ); } 
			            catch ( SQLException sqlException ) 
			            {
			            	JOptionPane.showMessageDialog( null, 
			                       sqlException.getMessage(), 
			                       "Database error", 
			                       JOptionPane.ERROR_MESSAGE 
			                );
			            }
		    		}
		    	}
		    	
		    	// client user
		    	else 
		    	{
		    		
		    		// if select, use setQuery()
		    		if (queryList[0].equalsIgnoreCase("select"))
		    		{
		            	// perform a new query
		                try 
		                { tableModel.setQuery( queryArea.getText() ); } 
		                catch ( SQLException sqlException ) 
		                {
		                	JOptionPane.showMessageDialog( null, 
		                           sqlException.getMessage(), 
		                           "Database error", 
		                           JOptionPane.ERROR_MESSAGE 
		                    );
		                }
		    		}
		    		
		    		// client user trying to update, print error message
		    		else { JOptionPane.showMessageDialog(
		    				null, 
		    				queryList[0] + " command denied to user '" + userField.getText() + "'@localhost' for table 'riders'");
		    		}
		    	}
	    	}
	    }); 
	
	
	    // create event listener for button
	    clearButton.addActionListener( 
	    	new ActionListener() 
	        {
	            public void actionPerformed( ActionEvent event )
	            {
		      		// clear text in SQL query from text area
		      		queryArea.setText("");
	            }
	        }          
	    ); 
	    
	
	    // create event listener for button
	    clearResultsButton.addActionListener( 
	       new ActionListener() 
	       {
	          // pass query to table model
	          public void actionPerformed( ActionEvent event )
	          {
	        	  try 
	        	  {
	        		  connection = connect();
	    	          tableModel = new ResultSetTableModel( connection );
	    	          resultTable.setModel(new DefaultTableModel());
	        	  } 
	        	  catch (ClassNotFoundException e) {e.printStackTrace();} 
	        	  catch (SQLException e) {e.printStackTrace();}
	        	  
	          } 
	       }          
	    ); 
	    
	  
	    // place GUI components on content pane
	    add( createTop(), BorderLayout.NORTH );
	    add( createMid(), BorderLayout.CENTER);
	    add( createBot(), BorderLayout.SOUTH );
	    setPreferredSize(new Dimension(1250, 700));
	    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    pack();
	    setVisible( true ); 
	
	    // ensure database connection is closed when user quits application
		addWindowListener(new WindowAdapter() 
			{
				// disconnect from database and exit when window has closed
			    public void windowClosed( WindowEvent event )
			    {
			    	tableModel.disconnectFromDatabase();
			        System.exit( 0 );
			    } 
			} 
		); 
	} 



	// method to to connect to database
	public Connection connect() throws SQLException
	{
	  	dataSource = new MysqlDataSource();
	  	dataSource.setURL((String) urlField.getSelectedItem());
	  	dataSource.setUser((String) userField.getText());
	  	//dataSource.setPassword((String) pwField.getText());
	  	char[] pw = pwField.getPassword();
	  	String pass = String.valueOf(pw);
	  	dataSource.setPassword(pass);
	
	    connection = dataSource.getConnection();
	
	    return connection;
	}

	
	// create top of GUI
	public Box createTop() 
	{
		// initialize settings
		topSettings();
		
		JPanel topHeaders = new JPanel(new GridLayout(1, 2));
		topHeaders.add(dbLabel);
		topHeaders.add(queryLabel);
		
		// components for top left part of GUI
		JPanel topLeft = new JPanel(new GridLayout(4, 2));
		topLeft.add(driver);
		topLeft.add(driverField); // comboBox
		topLeft.add(url);
		topLeft.add(urlField); // comboBox
		topLeft.add(user);
		topLeft.add(userField);
		topLeft.add(pw);
		topLeft.add(pwField);
		
		// components for top left
		JPanel top = new JPanel(new GridLayout(1, 2));
		top.add(topLeft);
		top.add(queryPane);
		
		// add everything for top portion to GUI
		Box topBox = Box.createVerticalBox();
		topBox.add(topHeaders);
		topBox.add(top);
		
		return topBox;
	}

	// create middle of GUI
	public Box createMid() 
	{
		// initialize settings
		midSettings();

	    // create box and add buttons
	    Box midBox = Box.createHorizontalBox();
	    midBox.add( status);
	    midBox.add( connectButton);
	    midBox.add( clearButton);
	    midBox.add( executeButton);
	    return midBox;
	}

	// create bottom of GUI
	public Box createBot()
	{
		// initialize settings
		botSettings();
	    
		// create JTable delegate for tableModel 
	    resultTable = new JTable( tableModel );
	    resultTable.setGridColor(Color.BLACK);

	    // add bottom contents to box
	    Box botBox = Box.createVerticalBox();
	    botBox.add(resultLabel);
	    botBox.add(new JScrollPane( resultTable));
	    botBox.add(clearResultsButton);
	    return botBox;
	}

	// initialize settings for all the top portion of GUI components
	private void topSettings()
	{
	    // text field labels for top portion of GUI
	    dbLabel.setFont(new Font("Arial", Font.BOLD, 20));
	    dbLabel.setForeground(Color.BLUE);
	    queryLabel.setFont(new Font("Arial", Font.BOLD, 20));
	    queryLabel.setForeground(Color.BLUE);
	    
	    driver.setFont(new Font("Arial", Font.BOLD, 10));
	    driver.setBackground(Color.DARK_GRAY);
	    url.setFont(new Font("Arial", Font.BOLD, 10));
	    url.setBackground(Color.DARK_GRAY);
	    user.setFont(new Font("Arial", Font.BOLD, 10));
	    user.setBackground(Color.DARK_GRAY);
	    pw.setFont(new Font("Arial", Font.BOLD, 10));
	    pw.setBackground(Color.DARK_GRAY);
	}

	// initialize settings for all the mid portion of GUI components
	private void midSettings()
	{
	    status = new JButton( " No Connection Status " );
		status.setEnabled(false);
	    status.setBackground(Color.BLACK);
	    status.setForeground(Color.RED);
	    status.setBorderPainted(false);
	    status.setOpaque(true);
	    connectButton.setBackground(Color.BLUE);
	    connectButton.setForeground(Color.YELLOW);
	    connectButton.setBorderPainted(false);
	    connectButton.setOpaque(true);
	    clearButton.setBackground(Color.WHITE);
	    clearButton.setForeground(Color.RED);
	    clearButton.setBorderPainted(false);
	    clearButton.setOpaque(true);
	    executeButton.setBackground(Color.GREEN);
	    executeButton.setForeground(Color.BLACK);
	    executeButton.setBorderPainted(false);
	    executeButton.setOpaque(true);
	}

	// initialize settings for all the bottom portion of GUI components
	private void botSettings()
	{
	    resultLabel.setFont(new Font("Serif", Font.BOLD, 20));
		resultLabel.setForeground(Color.BLUE);
	    clearResultsButton.setBackground(Color.YELLOW);
	    clearResultsButton.setForeground(Color.BLACK);
	    clearResultsButton.setBorderPainted(false);
	    clearResultsButton.setOpaque(true);
	}


	
	// execute application
	public static void main( String args[] ) 
	{
		new UI();	 
	}

} 

