package model;

import io.swagger.client.model.LiftRide;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LiftRideEvent {
  LiftRide liftRide;
  int resortID;
  String seasonID;
  String dayID;
  int skierID;

}
