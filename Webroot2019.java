import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class Player { 
    
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int bustersPerPlayer = in.nextInt(); // the amount of busters you control
        int ghostCount = in.nextInt(); // the amount of ghosts on the map
        int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right       
        
        // Data structures
        ArrayList<Ghost> ghost;
        ArrayList<Ghost> pastGhost = new ArrayList<Ghost>();
        Buster[] buster;
        
        // Base stuff
        double baseX, baseY, enemyBaseX, enemyBaseY;
        if (myTeamId == 0 ) {
            baseX = 0;
            baseY = 0;
            enemyBaseX = 16000;
            enemyBaseY = 9000;
        } else {
            baseX = 16000;
            baseY = 9000;
            enemyBaseX = 0;
            enemyBaseY = 0;
        } 
        
        // Random initiations
       
        Ghost ghostToBePrinted = null;
        
        boolean canBust = false;
        boolean canCatch = false;
        boolean canSteal = false;
        boolean canRelease = false;
        boolean pastListUsed = false;
        boolean canStun = false;
      
        boolean hunterMoveRight = true;
        
        
        
        // game loop
        while (true) {
            int entities = in.nextInt(); // the number of busters and ghosts visible to you
            ghost = new ArrayList<Ghost>(); //ArrayList of visible ghosts
            buster = new Buster[6];

            
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // buster id or ghost id
                int x = in.nextInt();
                int y = in.nextInt(); // position of this buster / ghost
                int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
                int entityRole = in.nextInt(); // -1 for ghosts, 0 for the HUNTER, 1 for the GHOST CATCHER and 2 for the SUPPORT
                int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost. For ghosts: remaining stamina points.
                int value = in.nextInt(); // For busters: Ghost id being carried/busted or number of turns left when stunned. 
                //For ghosts: number of busters attempting to trap this ghost.
                 
                // Save Ghost in Ghost[] 
                if (entityRole == -1 ) {                
                    ghost.add( new Ghost( x, y, entityId, state ) );
                }
                
                //Hunter
                if (entityRole == 0) {
                    if (entityType == myTeamId) {
                        buster[0] = new Buster( x, y, entityRole, state, true, entityId );
                    } else {
                        buster[3] = new Buster( x, y, entityRole, state, false, entityId );
                    }
                }    
                
                //Catcher
                if (entityRole == 1) {
                    if (entityType == myTeamId) {
                        buster[1] = new Buster( x, y, entityRole, state, true, entityId );
                    } else {
                        buster[4] = new Buster( x, y, entityRole, state, false, entityId );
                    }
             
                }   
                
                //Support
                if (entityRole == 2) {
                     if (entityType == myTeamId) {
                        buster[2] = new Buster( x, y, entityRole, state, true, entityId );
                    } else {
                        buster[5] = new Buster( x, y, entityRole, state, false, entityId );
                    }
         }                    
            
            }// End of for loop
            
            
            
            
            
            double ghost1X, ghost1Y;
            double ghost2X, ghost2Y;
            double ghostStatus;
            double ghostStatus2;
              
            // Finds closest/fastest to kill Ghost for Hunter (and Catcher) ------------------------------------------
            
            // Use ghost
            if (ghost.size() != 0) {
                //finding ghost for hunter
                Ghost nearestGhost = ghost.get(0);   
                  
                for (int j = 0; j < ghost.size(); j++) {
                    if ( ( (nearestGhost.getState() + ( nearestGhost.distToGhost(buster[0].getX(), buster[0].getY()) / 800 ) > 
                           (ghost.get(j).getState() + (ghost.get(j).distToGhost(buster[0].getX(), buster[0].getY()))) &&
                         (ghost.get(j).getState() != 0) ) ) ) {
                                    
                        nearestGhost = ghost.get(j);   
                    }
                }
                            
                ghostToBePrinted = nearestGhost; 
                ghost1X = nearestGhost.getX();
                ghost1Y = nearestGhost.getY();
                ghostStatus = nearestGhost.getState();
                pastListUsed = false;
                
                
                
                //find wandering ghost for catcher
                Ghost bustGhost = null;
                for (int j = 0; j < ghost.size(); j++) {
                    if (ghost.get(j).getState() <= 0) {
                        bustGhost = ghost.get(j);
                    }
                } 
                if (bustGhost == null) {
                    bustGhost = nearestGhost;
                }
                
                ghost2X = bustGhost.getX();
                ghost2Y = bustGhost.getY();
                ghostStatus2 = bustGhost.getState();
                
                
                
            // Use pastGhost
            } else if (pastGhost.size() != 0) {
                Ghost nearestGhost = pastGhost.get(0);
           
                for (int j = 0; j < pastGhost.size(); j++) {
                    if ( ( (nearestGhost.getState() + ( nearestGhost.distToGhost(buster[0].getX(), buster[0].getY()) / 800 ) > 
                           (pastGhost.get(j).getState() + (pastGhost.get(j).distToGhost(buster[0].getX(), buster[0].getY()))) &&
                         (pastGhost.get(j).getState() != 0) ) ) ) {
                        
                        nearestGhost = pastGhost.get(j);   
                    }
                }
                            
                ghostToBePrinted = nearestGhost; 
                ghost1X = ghost2X = nearestGhost.getX();
                ghost1Y = ghost2Y = nearestGhost.getY();
                ghostStatus = ghostStatus2 = nearestGhost.getState();
                pastListUsed = true;
                
            // Both ghost and pastGhost are size 0
            } else {
                ghostStatus = ghostStatus2 = 100;
                ghost1X = 0;
                ghost1Y = 0;
                ghost2X = 0;
                ghost2Y = 0;
            }
                        
                   
                   
                        
                        
            // Moving the HUNTER towards ghost - distance if ghost is within range
            if (ghost1X != 0 && ghost1Y != 0 && ghostStatus < 30) {
                double theta = Math.atan( (ghost1Y - buster[0].getY()) / (ghost1X - buster[0].getX()));
                buster[0].addX( (Math.hypot(ghost1X - buster[0].getX(), ghost1Y - buster[0].getY()) - 800) * 
                                Math.cos(theta) );
                buster[0].addY( (Math.hypot(ghost1X - buster[0].getX(), ghost1Y - buster[0].getY()) - 800) * 
                                Math.sin(theta) );
            } else {
                // Random movement
                if (buster[0].getX() > 14500) {
                    hunterMoveRight = false;
                }
                if (buster[0].getX() < 1500) {
                    hunterMoveRight = true;
                }
            
                if (hunterMoveRight) {
                    buster[0].addX( 330 );
                    buster[0].addY( 330 * ( 2 * Math.random() - 1) );
                } else {
                    buster[0].addX( -330 );
                    buster[0].addY( 330 * ( 2 * Math.random() - 1) );                      
                }
                                   
            }
            
            // HUNTER - bust ghost
            if (ghost1X != 0 && ghost1Y != 0 && withinRange(ghost1X, ghost1Y, buster[0].getX(), buster[0].getY(), 
                1760) && ghostStatus > 0 && pastListUsed == false) {
                canBust = true;
            } else {
                canBust = false;
            }
            
            
            
            
            
            // -----------------------------------------------------------------------------------------------------------------------------------
            
            // Finds closest dead/almost dead Ghost to Catcher
            
            // Moving the CATCHER towards ghost if within range
            if (ghost2X != 0 && ghost2Y != 0 && ghostStatus < 4 && withinRange(ghost2X, ghost2Y, buster[1].getX(), 
                buster[1].getY(), 2000)) {
                double theta = Math.atan( (ghost2Y - buster[1].getY()) / (ghost2X - buster[1].getX()));
                buster[1].addX( (Math.hypot(ghost2X - buster[1].getX(), ghost2Y - buster[1].getY()) - 1500) * 
                                Math.cos(theta) );
                buster[1].addY( (Math.hypot(ghost2X - buster[1].getX(), ghost2Y - buster[1].getY()) - 1500) * 
                                Math.sin(theta) );
                                
            } else if (buster[4] != null && buster[4].getState() == 2 ) {
            // Enemy catcher is stunned
                buster[1].setX( buster[2].getX() );
                buster[1].setY( buster[2].getY() );
                            
            } else {
            // Go to middle 
                buster[1].setX(8000);
                buster[1].setY(4500);
            }
            
            
            // CATCHER - catch ghost, within range of ghost
            if (ghost1X != 0 && ghost1Y != 0 && withinRange(ghost1X, ghost1Y, buster[1].getX(), buster[1].getY(), 
                1760) && ghostStatus == 0 && pastListUsed == false) {
                canCatch = true;
            } else if (ghost2X != 0 && ghost2Y != 0 && withinRange(ghost2X, ghost2Y, buster[1].getX(),         
                       buster[1].getY(), 1760) && ghostStatus2 == 0) {
                canCatch = true;
            }
            
            
            // CATCHER - release ghost
            if (buster[1].getState() == 1) {
                if (withinRange(buster[1].getX(), buster[1].getY(), baseX, baseY, 1600) ) {
                    canRelease = true;
                // Go back to base
                } else {
                    buster[1].setX( baseX );
                    buster[1].setY( baseY );
                }
            }
              
              
              
              
            //----------------------------------------------------------------------------------------------------------------------
           
            // SUPPORT - movement
            // Support camps enemy base
            if ( enemyBaseY == 0 ) {
                buster[2].setX( 1250 );
                buster[2].setY( 1250 ); 
            } else {
                buster[2].setX( enemyBaseX - 1250 );
                buster[2].setY( enemyBaseY - 1250 );
            }
            
            // Support chases enemy catcher when they have a ghost
            if ( buster[4] != null ) {
                buster[2].setX( buster[4].getX() );
                buster[2].setY( buster[4].getY() );
            }
            // Support Stuns when they are in range and has a ghost
            if ( buster[4] != null && buster[4].getState() == 1 ) {
                canStun = true;
            }
            

            
            
            
            
            

            // First the HUNTER : MOVE x y | BUST id
            // Second the GHOST CATCHER: MOVE x y | TRAP id | RELEASE
            // Third the SUPPORT: MOVE x y | STUN id | RADAR
            
            // -------------------------------------------------------------------------------------------------------
            
            // HUNTER ACTIONS -------------------------------------------------------------------
            if (canBust) {
            //Hunter busts
                System.out.println("BUST " + Math.round(ghostToBePrinted.getID()));
            
            //Hunter moves
            } else {
                if (ghostToBePrinted != null) {
                    System.out.println("MOVE " + Math.round(buster[0].getX()) + " " + Math.round(buster[0].getY()) +  
                                       " " + ghostToBePrinted.getID() + ", " + ghostToBePrinted.getX());
                }
            }
            
            // CATCHER ACTIONS ------------------------------------------------------------------
            if (canCatch && buster[1].getState() != 1) {
                //Catcher catches the ghost
                System.out.println("TRAP " + Math.round(ghostToBePrinted.getID()));
                canCatch = false;
                
            } else {
                //Goes back to base to release ghost
                if (buster[1].getState() == 1) {
                
                    //If at base
                    if (canRelease) {
                        System.out.println("RELEASE");
                        canRelease = false;
                        
                    //Go to base/move command
                    } else {
                        System.out.println("MOVE " + Math.round(buster[1].getX()) + " " + Math.round(buster[1].getY()));                
                    }
                    
                } else {
                    System.out.println("MOVE " + Math.round(buster[1].getX()) + " " + Math.round(buster[1].getY()));
                }
            }
            
            
            // SUPPORT ACTIONS ----------------------------------------------------------------------
            if (canStun && buster[4] != null) {
                System.out.println("STUN " + Math.round(buster[4].getId()));
                canStun = false;
            } else {
                System.out.println("MOVE " + Math.round(buster[2].getX()) + " " + Math.round(buster[2].getY()));
            }
            
            // Save pastGhost
            pastGhost = ghost;
            
            
        } //end of while loop
    }//end of main()
    
    
    public static boolean withinRange( double x1, double y1, double x2, double y2, int range) {
        return Math.hypot(x2 - x1, y2 - y1) <= range;
    }
    
}

class Ghost {
    
       double x;
       double y;
       double ghostID;
       double state;
       
       Ghost( double aX, double aY, double id, double aState ) {
           x = aX;
           y = aY;
           ghostID = id; 
           state = aState;
           
       }
       
       double getX() {
           return x;
       }
       double getY() {
           return y;
       }
       double getID() {
           return ghostID;
       }
       double getState() {
           return state;
       }
       boolean outofStam() {
           return state <= 0;
       }
       double distToGhost( double entityX, double entityY ) {
           return Math.hypot( entityX - x, entityY - y );
       } 
}



class Buster {

    double x;
    double y;
    int role;
    int state;
    boolean sameTeam;
    int entityId;
    
    Buster( double aX, double aY, int aRole, int aState, boolean aTeam, int id ) {
        x = aX;
        y = aY;
        role = aRole;
        state = aState;
        sameTeam = aTeam;
        entityId = id;
    }
    
    double getX() {
        return x;
    }
    
    double getY() {
        return y;
    }
    
    void addX(double moveX) {
        x += moveX;
    }
    
    void addY(double moveY) {
        y += moveY;
    }
    
    void setX(double newX) {
        x = newX;
    }
    
    void setY(double newY) {
        y = newY;
    }
    
    int getRole() {
        return role;
    }
    
    int getState() {
        return state;
    }
    
    boolean getTeam() {
        return sameTeam;
    }
    
    int getId() {
        return entityId;
    }

}



