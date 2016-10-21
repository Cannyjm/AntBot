
unsigned long time_ = 0;
boolean backwards = false;
boolean updatePosition = true;
int encoder1 = 0, encoder2 = 0, encoder3 = 0, encoder4 = 0;
int wheel_1 = 0, wheel_2 = 0, wheel_3 = 0, wheel_4 = 0;
unsigned long timer = 50;
boolean forward = true;

void moving(float distance){
    updatePosition = true;
    finished = false;
    forward=true;
    int dest_steps = abs(distance*1800);
    int stepcount = 0;
    wheel_1_encoder.write(0);
    wheel_2_encoder.write(0);
    wheel_3_encoder.write(0);
    wheel_4_encoder.write(0);
    if(distance > 0){
      digitalWrite(WHEEL_1_DIR, LOW);
      digitalWrite(WHEEL_2_DIR, LOW);
      digitalWrite(WHEEL_3_DIR, LOW);
      digitalWrite(WHEEL_4_DIR, LOW);
      backwards = false;
    } else if (distance < 0){
      digitalWrite(WHEEL_1_DIR, HIGH);
      digitalWrite(WHEEL_2_DIR, HIGH);
      digitalWrite(WHEEL_3_DIR, HIGH);
      digitalWrite(WHEEL_4_DIR, HIGH);
      backwards = true;
    } else {
      finished = true;
    }
    if(!finished){
      wheel_1 = 108;
      wheel_2 = 90;
      wheel_3 = 90;
      wheel_4 = 108;
      setWheelSpeed(wheel_1,wheel_2,wheel_3,wheel_4);
    }
    time_ = millis();
    while(!finished){
      encoder1 = abs(wheel_1_encoder.read());
      encoder2 = abs(wheel_2_encoder.read());
      encoder3 = abs(wheel_3_encoder.read());
      encoder4 = abs(wheel_4_encoder.read());
      if(!backwards){
        stepcount = (encoder1 + encoder3 + encoder4) / 3;
      } else{
        stepcount = (encoder1 + encoder2 + encoder4) / 3;
      }
      if(stepcount < dest_steps){
        finished = false;
      } else {
        finished = true;
      }
      if(stepcount % 18 == 0 && updatePosition){
        if(!backwards){
          Serial.print("m ");
          Serial.print(1);
          Serial.println(" n");
        } else {
          Serial.print("m ");
          Serial.print(-1);
          Serial.println(" n");
        }
        updatePosition = false;
      } else if (stepcount % 18 != 0){
        updatePosition = true;
      }
      if(time_ <= millis() - timer){
        adjustSpeed();
        time_ = millis();
      }
      checkHeartbeat();
      checkIncomingMessage();
    }
    checkIncomingMessage();
    Serial.println("j n");
    setWheelSpeed(0,0,0,0);
  }


  void turning(float angle){
    updatePosition = true;
    finished = false;
    forward=true;
    int dest_steps = abs(angle*1500)/360;
    int stepcount = 0;
    wheel_1_encoder.write(0);
    wheel_2_encoder.write(0);
    wheel_3_encoder.write(0);
    wheel_4_encoder.write(0);
    if(angle > 0){
      digitalWrite(WHEEL_1_DIR, LOW);
      digitalWrite(WHEEL_2_DIR, HIGH);
      digitalWrite(WHEEL_3_DIR, HIGH);
      digitalWrite(WHEEL_4_DIR, LOW);
      backwards = true;
    } else if (angle < 0){
      digitalWrite(WHEEL_1_DIR, HIGH);
      digitalWrite(WHEEL_2_DIR, LOW);
      digitalWrite(WHEEL_3_DIR, LOW);
      digitalWrite(WHEEL_4_DIR, HIGH);
      backwards = false;
    } else {
      finished = true;
    }
    if(!finished){
      wheel_1 = 100;
      wheel_2 = 120;
      wheel_3 = 120;
      wheel_4 = 100;
      setWheelSpeed(wheel_1,wheel_2,wheel_3,wheel_4);
    }
    time_ = millis();
    while(!finished){
      encoder1 = abs(wheel_1_encoder.read());
      encoder2 = abs(wheel_2_encoder.read());
      encoder3 = abs(wheel_3_encoder.read());
      encoder4 = abs(wheel_4_encoder.read());
      if(!backwards){
        stepcount = (encoder3 + encoder2)*1.22 / 2;
      } else{
        stepcount = (encoder3 + encoder2)*1.16 / 2;
      }
      if(stepcount < dest_steps){
        finished = false;
      } else {
        finished = true;
      }
      if(stepcount % 5 == 0 && updatePosition){
        if(backwards){
          Serial.print("t ");
          Serial.print(1.2);
          Serial.println(" n");
        } else {
          Serial.print("t ");
          Serial.print(-1.11);
          Serial.println(" n");
        }
        updatePosition = false;
      } else if (stepcount % 5 != 0){
        updatePosition = true;
      }
      if(time_ <= millis() - timer){
        adjustSpeed();
        time_ = millis();
      }
      checkHeartbeat();
      checkIncomingMessage();
    }
    checkIncomingMessage();
    Serial.println("j n");
    setWheelSpeed(0,0,0,0);
  }
  
  
    int E1, E2, E3, E4;
    int E1_old = 0, E2_old = 0, E3_old = 0, E4_old = 0;
    int E_left, E_right;
  void adjustSpeed(){
    
    E1 = abs(abs(wheel_1_encoder.read()) - E1_old);
    E2 = abs(abs(wheel_2_encoder.read()) - E2_old);
    E3 = abs(abs(wheel_3_encoder.read()) - E3_old);
    E4 = abs(abs(wheel_4_encoder.read()) - E4_old);
    E4 = (E4+E1)/2;
    if(backwards){
      E3 = E2;
    }
    
    
    E1_old = abs(wheel_1_encoder.read());
    E2_old = abs(wheel_2_encoder.read());
    E3_old = abs(wheel_3_encoder.read());
    E4_old = abs(wheel_4_encoder.read());
    if(backwards){
      E_left = E2_old;
    } else {
      E_left = E3_old;
    }
    
    E_right = (E4_old+E1_old)/2;
    if(forward){
       if((1.02*E_left > E_right + 2 || 1.02*E_left < E_right -2)){
      if(1.02*E_left > E_right + 2){
        wheel_1++;
        wheel_4++;
      } else {
        wheel_1--;
        wheel_4--;
      }
    } else if(1.02*E3 > E4+2 || 1.02*E3 < E4-2){
      if(1.02*E3 > E4+2){
        wheel_1++;
        wheel_4++;
      } else {
        wheel_1--;
        wheel_4--;
      }
    }else{
      if((E_left > E_right + 2 || E_left < E_right -2)){
      if(E_left > E_right + 2){
        wheel_1++;
        wheel_4++;
      } else {
        wheel_1--;
        wheel_4--;
      }
    } else if(E3 > E4+2 || E3 < E4-2){
      if(E3 > E4+2){
        wheel_1++;
        wheel_4++;
      } else {
        wheel_1--;
        wheel_4--;
      }
    }
    }
    }
    

    if((E4 + E3)/2 >= 16){
      wheel_1--;
      wheel_2--;
      wheel_3--;
      wheel_4--;
    } else {
      wheel_1++;
      wheel_2++;
      wheel_3++;
      wheel_4++;
    }

  if(leftCurve){
    wheel_1 = 120;
    wheel_2 = 50;
    wheel_3 = 50;
    wheel_4 = 120;
  }
  if(rightCurve){
    wheel_1 = 50;
    wheel_2 = 120;
    wheel_3 = 120;
    wheel_4 = 50;
    
  }
    
    setWheelSpeed(wheel_1, wheel_2, wheel_3, wheel_4);
  }


  void setWheelSpeed(int W1, int W2, int W3, int W4){
    analogWrite(WHEEL_1_PWM, W1);
    analogWrite(WHEEL_2_PWM, W2);
    analogWrite(WHEEL_3_PWM, W3);
    analogWrite(WHEEL_4_PWM, W4);
  }


