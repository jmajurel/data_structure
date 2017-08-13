import scalax.collection.Graph
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._
import org.apache.poi.ss.usermodel.{WorkbookFactory, DataFormatter}
import java.io.{File, FileOutputStream}

object GraphPratice{

  def main(args:Array[String]) {

    import scala.collection.JavaConversions._

    case class Task(name:String, value:Double)

    val workbook = WorkbookFactory.create(new File("inputfile.xlsx"))
    val sheet = workbook.getSheetAt(0)
    var row = 0
    var mymap:Map[String,Task] = Map()
    val g = Graph[Task,DiEdge]()
    for (row <- 1 to sheet.getPhysicalNumberOfRows()
    if (sheet.getRow(row) !=null)){
      var currentrow = sheet.getRow(row)
      val newTask = Task(currentrow.getCell(0).getStringCellValue(),currentrow.getCell(2).getNumericCellValue())
      mymap+=(currentrow.getCell(0).getStringCellValue()-> newTask)
      val predecessor = currentrow.getCell(1).getStringCellValue()
    }
    println("mymap:"+mymap)
    /*case class Fruit(name:String)
    val myfruits = Map("Banana"-> Fruit("Banana"), "Apple" -> Fruit("Apple"))
    val g = Graph(myfruits("Banana")~>myfruits("Apple"))*/
  }
}
