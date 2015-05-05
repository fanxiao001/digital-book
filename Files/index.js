window.onload = loadScript;

var geocoder;
var zoomlevel=[180,174,138.8,79.57,48.32,24.682,12.409,6.213,3.108,1.554,0.7741,0.387,0.1935,0.0968,0.0484,0.0242,0.0121,0.00605,0.00303];

//load when window is loaded
function loadScript() {
  
  var script = document.createElement('script');
  script.type = 'text/javascript';
  script.src = 'https://maps.googleapis.com/maps/api/js?v=3.exp' +
      '&signed_in=false&callback=initialize'; //call function initialize()
  document.body.appendChild(script);

  $("place[about]").css({'background-color':'red'});
  $("place[about]").bind("click",showMapOfPlace);
  $("span[property='vcard:hasAddress']").bind('click',showMapOfAddress);
  loadDialog();
  
}

//initialize Geocoder
function initialize() {
  var longitude;
  var latitude;
  geocoder = new google.maps.Geocoder();
  var mapOptions = {
    zoom: 10,
    center: new google.maps.LatLng(latitude, longitude),
    zoomControl: true,
    zoomControlOptions: {
      style: google.maps.ZoomControlStyle.LARGE
    }
  };
}

//show map of annotation item
function showMapOfPlace(){

  //get attributs
  var longitude=$(this).attr("long");
  var latitude=$(this).attr("lat");
  var boarding=$(this).attr("boarding");
  var res=boarding.split(",");
  var minus=res[3]-res[2];
  var zoom=19;

  //decide the zoom level according to boarding box
  for(var i=0;i<zoomlevel.length-1;i++){
    if(minus<=zoomlevel[i]&&minus>zoomlevel[i+1]) {
      zoom=i+1;
      break;
    }
  }

  var width=$(window).width();
  var myHref = "http://cartosm.eu/map?lon="+longitude+"&lat="+latitude+"&zoom="+zoom+"&width="+width+"&height=500&mark=true&nav=true&pan=true&zb=bar&style=default&icon=down";

  //pop up
  $.colorbox({
  width: "80%", 
  height: "80%", 
  iframe: true, 
  href: myHref, 
  opacity: 0.6
  });             
}

//show map of producter address
function showMapOfAddress(){

  var address=$(this).text();

  geocoder.geocode( { 'address': address}, function(results, status) {
    if (status == google.maps.GeocoderStatus.OK) {
      var longitude=results[0].geometry.location.lng();
      var latitude =results[0].geometry.location.lat();
      var myHref = "http://cartosm.eu/map?lon="+longitude+"&lat="+latitude+"&zoom=18&width=700&height=500&mark=true&nav=true&pan=true&zb=bar&style=default&icon=down";
      $.colorbox({
        width: "90%", 
        height: "90%", 
        iframe: true, 
        href: myHref, 
            opacity: 0.6
        });
      } else {
        alert("Geocode was not successful for the following reason: " + status);
      }
    });
}

//show dialog to get time and date when click on a visit time of producter
function loadDialog(){
    //show dialog to get date and time
    var dialog,form;
    dialog = $( "#dialog-form" ).dialog({
      autoOpen: false,
      height: 300,
      width: 350,
      modal: true,
      buttons: {
        "Find": FindProduct
      },
      close: function( event, ui ) {}
    });
 
    form = dialog.find( "form" ).on( "submit", function( event ) {
      event.preventDefault();
      FindProduct(); // call function FindProduct()
    });
 
    $( "time[horaire]" ).click(function() {
      dialog.dialog( "open" );
    });
}

//find producter 
function FindProduct() {

  var week={}; //hash week and index
  week['dim.']=0;
  week['lun.']=1;
  week['mar.']=2;
  week['mer.']=3;
  week['jeu.']=4;
  week['ven.']=5;
  week['sam.']=6;
  
  var dateChoosed,time; //the day and time choosed
  //get date and time
  dateChoosed = $( "#getDate" ).val(), //yyyy-mm-dd
  time = $( "#getTime" ).val(); //09:30, 21:30
  time=time.replace(":","h"); //convert to 09h30
  time=time.replace("00",""); //abandon 00

  var d=new Date(dateChoosed);
  var day=d.getDay();// which day of week
  var date=d.getDate();//dd
  var month=d.getMonth();//mm
  var year=d.getFullYear();////yyyy

  if(dateChoosed==null||dateChoosed==""||time==null||time=="") {
    alert("please choose a date and time");
  }else{
    //for each visit time 
    $('time[horaire]').each(function() {
      var item=$(this);
      $(this).css({'background-color':'green'}); //reset the background color
      var horaire=$.trim($(this).text()); //ignore begin and end espace

      var intervals=horaire.split(";");

      if(intervals.length>=2){
        $.each(intervals,function(index,value){ 
        value=$.trim(value); //may contains f. or other cases
          if(value=="f. dern. semaine d'août"&&month==7&&date>=25){
            item.css({'background-color':'white'});
            intervals.splice(index,1);
            return false;
          }
          if(value=="f. sem. 15 août"&&month==7){
            var someday=new Date(year,8,15);
            var someday_day=someday.getDay();
            if(date>=(15-someday_day)&&date<=(21-someday_day)){
              item.css({'background-color':'white'});
              intervals.splice(index,1);
              return false;
            }              
          }
          if(value=="f. 16-31 août"&&month==7&&date>=16&&date<=31){
              item.css({'background-color':'white'});
              intervals.splice(index,1);
              return false;
          }
          if(value=="f. août"&&month==7){
            item.css({'background-color':'white'});
            intervals.splice(index,1);
            return false;
          }
          if(value=="f. 1er -15 août"&&month==7&&date>=1&&date<=15){
            item.css({'background-color':'white'});
            intervals.splice(index,1);
            return false;
          }

        });
      }

      if(intervals.length>=2){
        var flag=false;
        var allDays=[0,1,2,3,4,5,6];
        for(var i=intervals.length-1;i>=0;i--){
          var days=[];
          var times=[];
          var splitParts=$.trim(intervals[i]).split(" "); //split string by whitespace
          $.each(splitParts,function(index,value){
            value=$.trim(value).toLowerCase();
            if(value=="t.l.j."){
              days=allDays;
            }
            if(week[value]>-1) {
            var p=allDays.indexOf(week[value]);
            days.push(week[value]); //week(sam. lun.)
            if(p>-1) allDays.splice(p,1);
          }
            if(value.indexOf("-")>-1) times.push(value);//time
            if(value=="r.-v.") times.push("0h0-24h0");
          });
          if(inPeriod(days,times,day,time)==true) flag=true;
        }
        if(flag==false) item.css({'background-color':'white'});
        
      }

      else{
        var interval=intervals[0];
        if(interval.indexOf("sf") > -1){ //contains sf
          var splitParts=interval.split("sf"); //split string by sf
          var days=[];
          var times=[];
          var beforeSF=splitParts[0].split(" "); //the part before sf
          var afterSF=splitParts[1].split(" ");//the part after sf
          
          $.each(beforeSF,function(index,value){
            value=$.trim(value).toLowerCase();
            if(value=="t.l.j.") days=days.concat([0,1,2,3,4,5,6]);//add day
          });
          //after sf. inverse, get ride from days
          $.each(afterSF,function(index,value){
            value=$.trim(value).toLowerCase();
            if(week[value]>-1) days.splice(week[value],1);//get ride of day
            if(value.indexOf("-")>-1) times.push(value);//add possible time
          });

          if(inPeriod(days,times,day,time)==false){
            item.css({'background-color':'white'});
          }
        }
        else{ //not contains sf.
          var splitParts=interval.split(" ");
          var days=[];
          var times=[];
          $.each(splitParts,function(index,value){
            value=$.trim(value).toLowerCase();
            if(value=="t.l.j.") days=days.concat([0,1,2,3,4,5,6]);
            if(value=="r.-v."){ //r.-v. 
              days=days.concat([0,1,2,3,4,5,6]);
              times.push("0h0-24h0");
            }
            if(week[value]>-1) days.push(week[value]); //week(sam. lun.)
            if(value.indexOf("-")>-1) times.push(value);//time(14h-18h)
          });
          if(inPeriod(days,times,day,time)==false){
            item.css({'background-color':'white'});
          }
        }
      }
    });
  }  
}

//check if a specific time is in the period given
function inPeriod(days,times,day,time){
    //the give day is not in the possible days
    var flag=false;
    if($.inArray(day,days)==-1){
      flag=false;
      return flag;
    }
    else{
        $.each(times,function(index,value){
          if(inPeriodTime(value,time)==true){
            flag=true;
            return false;  //only break from each loop, not this function
          }    
        });
        return flag;
    }
}

//check if a specific time is within a time period
function inPeriodTime(time, givenTime){
  var flag;
  var parts=time.split("-");
  parts.push(givenTime);
  var begin, end, giventime;
  $.each(parts,function(index,value){
    var t;
    if(value.indexOf("h")==(value.length-1)){ //9h convert to int
     t=parseInt(value.substring(0,value.indexOf("h")))*60;
    }
    else{//9h30 convert to int

      t=parseInt(value.substring(0,value.indexOf("h")))*60+parseInt(value.substring(value.indexOf("h")+1,value.length));
    }
    if(index==0) begin=t;
    if(index==1) end=t;
    if(index==2) giventime=t;
  });
  if(giventime>=begin&&giventime<=end) flag=true; //in time period
  else flag=false;
  return flag;

}





