package mud
import scala.io.StdIn
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.actor.PoisonPill

class Player(val id:String,private var name: String, private var description: String, private var location: ActorRef, private var inventory: List[Item]) extends Actor{
  import Player._
  var in=""
  def receive={
    case PrintMessage(message:String)=>Thread.sleep(10)
      printMessage(message)
    case TakeExit(optRoom:Option[ActorRef])=>
      takeExit(optRoom)
    case TakeItem(item:Item)=>
      takeItem(item)
    case CreatePlayer(room:ActorRef,id:String)=>
      createPlayer(room,id)
    case ProcessInput =>
      var cmd=readLine
      parseCommand(cmd,id)
    case m=>
      println("uh oh whoopsie " +m)
      
  }
  def printMessage(msg:String):Unit={
    println(msg)
  }
  def parseCommand(cmd:String,sender:String):Unit = {
    printMessage("\n")
    if (cmd == "s" || cmd == "n" || cmd == "e" || cmd == "w" || cmd == "u" || cmd == "d") {
      move(cmd,sender)
    } else if (cmd == "look") {
      location ! Room.GetDescription
      printMessage(in)
    } else if (cmd == "inv") {
      printInv
    } else if (cmd.startsWith("get")) {
      var toGet = cmd.split("t ")(1)
      location ! Room.GetItem(toGet)
    } else if (cmd.startsWith("drop")) {
      var toDrop = cmd.split("p ")(1)
      var it=inventory.filter(_.name==toDrop)(0)
      location ! Room.DropItem(it)
      inventory=inventory.filter(_.name==toDrop)
    } else if (cmd == "help") {
      printMessage("COMMANDS: \n \n n, s, e, w, u, d - moves player \n \n look - reprints description of current room \n inv - lists current inventory \n get [item] - grab item from the room and add to your inventory \n drop [item] - drops an item from your inventory and puts it in the room \n exit - exits the game. Your data will not be saved. It is worth nothing.")
    } else {
      printMessage("Invalid command! Type 'help' for a list of all available commands.")
    }
    printMessage("\n")
  }

  def move(d: String,sender:String): Unit = {
    val dir = d.trim
    if (dir == "n") {
      location ! Room.GetExit(0)
    } else if (dir == "s") {
      location ! Room.GetExit(1)
    } else if (dir == "e") {
      location ! Room.GetExit(2)
    } else if (dir == "w") {
      location ! Room.GetExit(3)
    } else if (dir == "u") {
      location ! Room.GetExit(4)
    } else if (dir == "d") {
      location ! Room.GetExit(5)
    }
      location ! Room.GetName
      location ! Room.GetDescription
  }
  def printInv(): Unit = {
    if (inventory == null||inventory.length==0) {
      printMessage("You don't have anything in your inventory! Type 'get [item name]' to grab something from the room.")
    } else {
      var toPrint = ""
      for (item <- inventory) {
        toPrint = toPrint + item.name + "\n||" + item.desc + "\n\n"
      }
      printMessage(toPrint)
    }
  }
  def takeExit(optRoom:Option[ActorRef])={
    if(optRoom==None){
      printMessage("There is no exit that way.")
    }
    else{
      location=optRoom.get
    }
  }
  def takeItem(item:Item)={
    val newInv:List[Item]=item::inventory
    inventory=newInv
  }
  def createPlayer(room:ActorRef,id:String)={
    printMessage("What's your clown name?\n")
    var name=readLine
    printMessage("What's your clown description?\n")
    var desc=readLine
    printMessage("COMMANDS: \n \n n, s, e, w, u, d - moves player \n \n look - reprints description of current room \n inv - lists current inventory \n get [item] - grab item from the room and add to your inventory \n drop [item] - drops an item from your inventory and puts it in the room \n exit - exits the game. Your data will not be saved. It is worth nothing.")
    Main.playerManage ! PlayerManager.NewPlayer(id,name,desc,room,List(new Item("Starter bow","Emblazoned on the front: 'Nerf or Nothing'")))

 
    
  }
}
object Player {
  case object ProcessInput
  case class PrintMessage(message:String)
  case class TakeExit(optRoom:Option[ActorRef])
  case class TakeItem(item:Item)
  case class CreatePlayer(room:ActorRef,id:String)
//  def initPlayer(): Player = {
//    println("What's your clown name?\n")
//    val name = readLine
//    println("\nWhat's your backstory?\n")
//    val description = readLine
//    println("\nNice. I'm Chad. My clown name is Chaddington III. Good to meet you bro.\nAlright, my first class is starting soon. See ya!\n")
//    println("[CHADDINGTON III waves goodbye. He has on a Gucci eyepatch. You don't want to ask questions.]")
//    val location = "Honksley_Hall"
//    println("\nType 'look' to look around. Type 'help' to see more commands.")
//    val inventory = List(Item("Catcher's glove", "You hate baseball, but the gloves make for good clown-punching armor."))
//    val player = new Player(name, description, location, inventory)
//    player
//  }
}