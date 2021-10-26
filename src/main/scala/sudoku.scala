package jdbc

import org.apache.spark.sql.execution.SQLExecution

import scala.io.StdIn.readLine
import java.sql.DriverManager
import java.sql.Connection
import scala.util.Random
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer

object sudoku{
  var r:Int =0
  var c: Int = 0
  println("Welcome to Soduko.")
  var hint_index : ListBuffer[(Int,Int)]= ListBuffer()
  val rules = "n1) every square needs a single number.\n2)only the numbers 1 - 4 can be used."+
    "\n3) each box has no duplicates\n4) the entire row and column has no duplicates\n 4) if you attempt to write a "+
    "duplicate value, counts as a mistake, 3 mistakes you lose\n 5)cannot overwrite hint value(does not count as mistake)"



  println("do you need the rules? Enter y/n")
  val user_rules = readLine()

  var newgame = false
  var table = 0
  println("Do you want to load a new game? Yes, enter save code, else 0")
  var usersave = scala.io.StdIn.readInt()
  if (usersave == 0) {
    println("What level of difficulty do you want? 1 2 or 3")
    table = scala.io.StdIn.readInt()
    newgame = true
    table match {
      case 1 => {
        r = 4
        c = 4
      }
      case 2 => {
        r = 6
        c = 6
      }
      case 3 => {
        r = 9
        c = 9
      }
    }
  }else{
    table = usersave
    table match {
      case 1 => {
        r = 4
        c = 4
      }
      case 2 => {
        r = 6
        c = 6
      }
      case 3 => {
        r = 9
        c = 9
      }

    }}
    val answer = Array.ofDim[Int](r, c)
    var user_array = Array.ofDim[Int](r, c)

  def load_board(row: Int, col: Int, value: Int, array:Array[Array[Int]])={
    """function to load the board with values from the database"""
    array(row)(col) = value
  }

  def print_board(array:Array[Array[Int]])={
    array.map(_.mkString(" ")).foreach(println)
  }

  def checkforzero(array:Array[Array[Int]])={
    """Make sure there are no empty values left"""
    var zero = false
    for(i<-0 until r; j<-0 until c) {
      if (array(i)(j) == 0) zero=true
    }
    zero
  }

  def unique (row:Int, col:Int, value :Int, array:Array[Array[Int]]): Boolean ={
    """Check if the user entered value is unigue in the given row and col"""
    var row_unique, col_unique = false
    var rowlist = new scala.collection.mutable.ListBuffer[Int]()
    for(i<- 0 until r){
      rowlist += array(i)(col)
    }
    if (rowlist.count(_==value) >= 1){
      row_unique = false
    } else{
      row_unique = true
    }
    var collist = new scala.collection.mutable.ListBuffer[Int]()
    for(i<- 0 until c){
      collist += array(row)(i)
    }
    if (collist.count(_==value) >= 1){
      col_unique = false
    } else{
      col_unique = true
    }
    //var cornerlist = new scala.collection.mutable.ListBuffer[Int]()

    row_unique && col_unique
  }




  def main(args: Array[String]){
    val driver = "com.mysql.cj.jdbc.Driver"
    val url= "jdbc:mysql://localhost/project0draft1"
    val username = "root"
    val password = System.getenv("JDBC_PASSWORD")
    var connection:Connection = null


    var num_hint: Int = r

    if(user_rules=="y"){
      print(rules)
    }

    try {
      // make the connection
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)

      // create the statement, and run the select query
      if(newgame) {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(s"SELECT rowss, col, val FROM puzzle WHERE puzzleID=$table order by rowss desc")


        while (resultSet.next()) {
          val rowss = (resultSet.getString("rowss")).toInt
          val col = resultSet.getString("col").toInt
          val value = resultSet.getString("val").toInt
          load_board(rowss, col, value, answer)
        }
      }else{
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(s"SELECT rowss, col, val FROM puzzle WHERE puzzleID=$table order by rowss desc")

        while (resultSet.next()) {
          val rowss = (resultSet.getString("rowss")).toInt
          val col = resultSet.getString("col").toInt
          val value = resultSet.getString("val").toInt
          load_board(rowss, col, value, answer)
        }
        val savedSet = statement.executeQuery(s"SELECT rowws, col, valued FROM user_puzzle WHERE user_puzzleID=$table order by rowws desc")
        while (savedSet.next()) {
          val rowws = (savedSet.getString("rowws")).toInt
          val col = savedSet.getString("col").toInt
          val value = savedSet.getString("valued").toInt
          //load_board(rowws, col, value, user_array)
          user_array(rowws)(col)=value
        }
      }
    } catch {
      case e:Throwable => e.printStackTrace
    }

    //answer.map(_.mkString(" ")).foreach(println)

    var hint_row, hint_col = 0;
    while(num_hint != 0 ){
      hint_row = scala.util.Random.nextInt(r)
      hint_col = scala.util.Random.nextInt(c)
      hint_index += ((hint_row,hint_col))
      user_array(hint_row)(hint_col) = answer(hint_row)(hint_col)
      num_hint -=1
    }

    print_board(user_array)

    var user_row, user_col, user_value = 0
    var mistake = 0

    while (checkforzero(user_array) && mistake != 3) {

      println("Enter your row")
      user_row = scala.io.StdIn.readInt()

      println("Enter your col")
      user_col = scala.io.StdIn.readInt()

      println("Enter your value")
      user_value = scala.io.StdIn.readInt()

      println(s"You entered ($user_row, $user_col) to be $user_value")
      while (hint_index.contains((user_row,user_col))) {
        println("Cannot overwrite hint value!")
        user_row = scala.io.StdIn.readInt()
        println("Enter column")
        user_col = scala.io.StdIn.readInt()
      }
      if(unique(user_row,user_col,user_value,user_array)== false){
        println("Oops, duplicate value!")
        mistake += 1
      }
      user_array(user_row)(user_col) = user_value
      print_board(user_array)
    }

    var user_save = ""
    var usev= 0
    if (mistake == 3){
      println("Made one too many mistakes!")
      println("Do you want to save your progress?y/n")
      val save = readLine()
      if (save == "y") {
        try {
          val statement = connection.createStatement()
          for (i <- 0 until r; j <- 0 until c) {
            usev = user_array(i)(j)
            user_save = s"INSERT INTO user_puzzle VALUES ($table, $i,$j,$usev)"
            statement.executeUpdate(user_save)
          }
        } catch {
          case e: Throwable => e.printStackTrace
        }
      }
    }
    if (user_array.deep == answer.deep){
      println("Congratulations you win ")
    }
    else{
      println("Sorry you lose")    }
    print_board(user_array)
  }


}


