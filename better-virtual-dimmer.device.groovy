/*****************************************************************************
Device:  VirtualBetterDimmer.device.groovy
Author:  twack@wackware.net
Version: 0.1
Date:    2013-11-16
Purpose: To implement horizontal slider and up/down toggle in virtual dimmer
         device. This is great device to use with "Dim With Me" app. This
         functionality can also be found in "BetterDimmer.app.groovy".

Use License: Non-Profit Open Software License version 3.0 (NPOSL-3.0)
             http://opensource.org/licenses/NPOSL-3.0

 ******************************************************************************
 *                                Changes
 ******************************************************************************
 *
 *  Change 1:	2013-11-16 (wackford)
 *				Initial Build
 *
 *  Change 2:	2014-10-10 (twackford)
 *				Rebuilt to add metadata
 *
 ******************************************************************************
                
  Other Info:	Special thanks to Danny Kleinman at ST for helping me get the
				state stuff figured out. The Android state filtering had me 
                stumped.

 *****************************************************************************/
 
preferences {
    input("stepsize", "text", title: "Step Size", description: "Step Size (default 10)")
}

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Better Virtual Dimmer", author: "todd@wackford.net") {
		capability "Switch"
        capability "Switch Level"
		capability "Refresh"
		capability "Polling"

		attribute "stepsize", "string"

		command "levelUp"
		command "levelDown"
		command "getLevel"
		command "dimmerOn"
		command "dimmerOff"
	}

    tiles {
        // had to overide standard off and on actions to work on Android.
        // screwed up the currentValue variable in the value tile
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"dimmerOff", icon:"st.switches.switch.on", backgroundColor:"#79b821"
            state "off", label:'${name}', action:"dimmerOn", icon:"st.switches.switch.off", backgroundColor:"#ffffff"

        }
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
            state "level", action:"switch level.setLevel", unit:"", backgroundColor:"#ffe71e"
        }
        
        valueTile("lValue", "device.level", inactiveLabel: true, height:1, width:1, decoration: "flat") {
            state "levelValue", label:'${currentValue}%', unit:"", backgroundColor: "#53a7c0"
        }

        standardTile("lUp", "device.switch", inactiveLabel: false,decoration: "flat", canChangeIcon: false) {
                        state "up", label:'', action:"levelUp",icon:"st.illuminance.illuminance.bright"
        }
        standardTile("lDown", "device.switch", inactiveLabel: false,decoration: "flat", canChangeIcon: false) {
                        state "down", label:'', action:"levelDown",icon:"st.illuminance.illuminance.light"
        }

        main(["switch"])
        details(["switch","lUp","lDown","levelSliderControl","lValue" ,"Preferences"])
    }
}

def parse(String description) {}

def dimmerOn() { //made our own, since event was filtered by default on Android
    log.info "on"
    sendEvent(name:"switch",value:"on")
}

def dimmerOff() { //made our own, since event was filtered by default on Android
    log.info "off"
    sendEvent(name:"switch",value:"off")
    
}

def setLevel(val){
    log.info "setLevel $val"
    log.info "Step Size: ${settings.stepsize}"
    
    // make sure we don't drive switches past allowed values (command will hang device waiting for it to
    // execute. Never commes back)
    if (val < 0){
    	val = 0
    }
    
    if( val > 100){
    	val = 100
    }
    
    if (val == 0){ // I liked that 0 = off
    	sendEvent(name:"level",value:val)
    	dimmerOff()
    }
    else
    {
    	dimmerOn()
    	sendEvent(name:"level",value:val)
    	sendEvent(name:"switch.setLevel",value:val) // had to add this to work if apps subscribed to
                                                    // setLevel event. "Dim With Me" was one.
    }
}

def levelUp(){
    int nextLevel = device.currentValue("level") + 10
    setLevel(nextLevel)
    log.info "level up $nextLevel"
}

def levelDown(){
    int nextLevel = device.currentValue("level") - 10
    setLevel(nextLevel)
    log.info "level down $nextLevel"
}

def setLevel(val, dur){ //not called, but leave here for hue and other controllables
  log.info "setLevel $val, $dur"
  sendEvent(name:"setLevel",value:val)
}

def getLevel(){ //not called, dunno why but I'll leave it for later playing
  log.info device.currentValue("level")
  log.info device.currentValue("switch")
}

def poll() {
    log.info "poll"
}

def refresh() {
    log.info "refresh"
}
