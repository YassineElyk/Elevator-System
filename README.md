# Elevator control System

This program is a simple simulation of an elevator control system that can handle one or more elevators.
It records passenger calls from any of the available floors and assigns them to one of the elevators. The program
implements a call response strategy for the elevator cabins to determine the order by which they respond to passenger calls.

## Algorithm

#### Call logic
In the program, call requests have a starting floor and a direction, whereas landing requests are supposed to be made from within 
the elevator and specify a landing floor only.  
The algorithm used for the call logic is the classic elevator algorithm (SCAN). It is based on the following rules:
- An idle elevator accepts any type of request at first and moves towards its floor in a certain direction.
- On its way, the elevator stops only for call requests that are in the same direction, for requests that are at the farthest point of its 
 current direction of travel, or for landing requests. 
- Once the furthest request reached, the elevator either becomes idle again if it has no requests left or reverses its direction and 
responds to the other requests following the same rules.

#### Scheduling logic
Each call request needs to be assigned to a particular elevator. We have chosen a simple scheduling algorithm (Nearest Car) based on calculating 
a score for each elevator called figure of suitability (SN). 
For a certain request received, SN is calculated according the the following rules:  

for d = |car floor − landing floor| and N the total number of floors in the system

- If the elevator is headed towards the corresponding floor and has the same direction, a position bias of one is given to this elevator thus:
SN = N + 1 - (d - 1) = N + 2 - d
- If the elevator is idle or headed towards the corresponding floor with an opposite direction:
SN = N + 1 - d
- If the elevator is moving away from the requestes floor then: 
SN = 1

In the program, in addition to selecting the most suitable elevators with the highest value of SN, we select among those who have equal values of SN, the elevator
that has the least number of requests.
 
## Implementation
This program is implemented using classic Akka actors to handle concurrent management of all elevators and their state.

Each elevator's state is fully determined by the ADT `ElevatorState` which is able to store information about the current furthest received requests (in both possible directions
of travel) and all other requests received by the elevator (note that for each floor we make it possible to store different directions and request type info considering that two passengers
may push different direction buttons on the same floor or might call that same floor from the inside of the elevator). This is possible through using a Map of type `Map[Floor, ReceivedRequestDirection]`
to store all the request info where `ReceivedRequestDirection` is a case class of boolean fields representing respectively wether the request is a landing request, a call request of direction up, or a call
request of direction down.
The evolution between different states of the system can happen either through manual stepping or automatic time stepping implemented with Akka's internal timers.  
  
The Manager actor communicates with the outside of the Actor system through the `ElevatorSystemController` and is in charge of delegating call requests to elevators by sorting them according to their suitability score and then by the number of received requests they have.
The Manager actor is also in charge of creating elevators and collecting their current states and presenting them to the UI.  

## Bundle and run application
Create an executable script to run the application using:  
```
sbt pack
```

go to target/pack/bin and run the executable script like so:
```
./ElevatorControlSystem 
```

## Usage

    Options:
    -f, --floorCount=<value>                     set number of floors in the system
    -e, --elevatorCount=<value>                  set number of elevators in the system
    -t, --travelDuration=<value>                 set duration of travel between two consecutive floors in seconds
    -w, --waitingDuration=<value>                set duration of passenger loading/unloading in seconds
    -m, --manualStepping=<value>                 set manual time stepping of system evolution
    
    Commands:
    status                                       show status of all the elevators in the system
    call <floorNumber> {up|down}                 schedule a call request in one of the elevators
    land <elevatorId> <floorNumber>              send a landing request when inside an elevator
    help                                         print this help
    exit                                         quit application

#### Example

Options passing:
```
./ElevatorControlSystem --floorCount=20 --elevatorCount=3 --travelDuration=30 --waitingDuration=10 --manualStepping=false
```

Interactive CLI example: 
```
Elevator simulation is running
-------------------------------------------------------------------
call 9 down
Call request to floor 9 with direction down was assigned to elevator: 1 
call 3 up
Call request to floor 3 with direction up was assigned to elevator: 1 
call 2 up
Call request to floor 2 with direction up was assigned to elevator: 1 
call 6 down
Call request to floor 6 with direction down was assigned to elevator: 0 
call 4 up
Call request to floor 4 with direction up was assigned to elevator: 0 

status

--------------------------------------------------------------------------------------
Elevator ID: 1
Waiting
Current floor: 2
Up

Requests:
Floor 3: Up || Down || 
Floor 9: Down || 

    
--------------------------------------------------------------------------------------
Elevator ID: 0
Moving
Next floor: 2
Up

Requests:
Floor 4: Up || 
Floor 6: Down || 
```
