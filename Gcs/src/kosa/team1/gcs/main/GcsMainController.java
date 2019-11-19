package kosa.team1.gcs.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;


import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import syk.common.MavJsonListener;
import syk.common.MavJsonMessage;
import syk.gcs.cameraview.CameraView;
import syk.gcs.cameraview.ImageListener;
import syk.gcs.dialog.AlertDialog;
import syk.gcs.hudview.Hud;
import syk.gcs.mapview.FlightMap;
import syk.gcs.mapview.MapListener;
import syk.gcs.messageview.MessageView;
//import syk.sample.gcs.network.Drone;
//import syk.sample.gcs.network.NetworkConfig;
import  kosa.team1.gcs.network.Drone;
import  kosa.team1.gcs.network.NetworkConfig;
import kosa.team1.gcs.main.Dronemanual.ServiceDialog04;
import kosa.team1.gcs.main.MissionRequesting.MissionRequesting;
import kosa.team1.gcs.main.DroneSelect.ServiceDroneSelect;


import java.net.URL;
import java.util.ResourceBundle;

public class GcsMainController implements Initializable {
	//---------------------------------------------------------------------------------
	private static Logger logger = LoggerFactory.getLogger(GcsMainController.class);
	//---------------------------------------------------------------------------------
	@FXML public Button btnConnectConfig;
	@FXML public Button btnConnect;
	@FXML public Button btnArm;
	@FXML public TextField txtTakeoffHeight;
	@FXML public Button btnTakeoff;
	@FXML public Button btnLand;
	@FXML public Button btnRtl;
	@FXML public Button btnManual;
	@FXML public CheckBox chkManualMove;
	@FXML public CheckBox chkManualAlt;
	@FXML public TextField txtManualAlt;
	@FXML public Button btnMissionMake;
	@FXML public Button btnMissionClear;
	@FXML public Button btnMissionUpload;
	@FXML public Button btnMissionDownload;
	@FXML public Button btnMissionStart;
	@FXML public Button btnMissionStop;
	@FXML public Button btnGetMissionFromFile;
	@FXML public Button btnSaveMissionToFile;
	@FXML public Button btnFenceMake;
	@FXML public Button btnFenceClear;
	@FXML public Button btnFenceUpload;
	@FXML public Button btnFenceDownload;
	@FXML public Button btnFenceEnable;
	@FXML public Button btnFenceDisable;
	@FXML public Button btnMessageView;
	@FXML public Button btnCameraView;
	@FXML public Button btnNorth;
	@FXML public Button btnSouth;
	@FXML public Button btnEast;
	@FXML public Button btnWest;

	// 상단 서비스
	@FXML public Button btnMissionReady;
	@FXML public Button btnRootSet;
	@FXML public Button btnPackage;
	@FXML public Button btnDroneSelect;
	// 생성자
	public GcsMainController(){
		fcMqttClient = null;
	}



	public Drone drone;

	private boolean FCMqttClientTrigger_GPS = false;
	private boolean FCMqttClientTrigger_Mission = false;

	// 미션을 받았다면 발동하여 그때부터 드론 GPS 좌표를 전송한다.
	private boolean WebMissionInTrigger = false;

	// 미션 세팅을 완료하였으면 이를 발생하여 FC에서 받은 정보를 안드로이드에 보내기 위함이다.
	private boolean MissionUploadTrigger = false;

	// Service04Dialog 가 작동되었는지 확인하기 위함. 0 : 미작. 1 : 작동완료
	private int DroneControllerTrigger = 0;

	// 안드로이드와 Web 에서 전송될 토픽 /gcs/main 에 대한 subscribe 되는 클라이언트 이다.
	// 또한 안드로이드와 Web 에 정보를 전달 역할을 수행 할 클라이언트이다.
	private GcsMainMqtt gcsMainMqtt;

	// /drone/fc/pub 에서 나오는 정보를 받아 실행 될 클라이언트
	private FCMqttClient fcMqttClient;
	// 실제로 EventHandler 와 연관되어 사용되진 않을 것이다.

	private double destinationLat;
	private double destinationLng;

	// gps 전송 Thread 의 생성에 제한을 주기 위함.
	private int gpsSendThread = 0;

	// 미션 수행 완료 트리거
	private int missionDone = 0;

	private int currentMissionNumber = -1;
	private int droneNumber = -1;

	// 미션 시작 전 : 0,  미션 시작 : 1, 미션 종료 : 2.
	private int missionCurrentSeqTrigger = 0;

	// 현재 드론 위치
	private String currLat;
	private String currLng;
	private String currAlt;
	//---------------------------------------------------------------------------------
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btnConnectConfig.setOnAction(btnConnectConfigEventHandler);
		btnConnect.setOnAction(btnConnectEventHandler);
		btnArm.setOnAction(btnArmEventHandler);
		btnTakeoff.setOnAction(btnTakeoffEventHandler); btnTakeoff.setDisable(true); 
		btnLand.setOnAction(btnLandEventHandler); btnLand.setDisable(true);
		btnRtl.setOnAction(btnRtlEventHandler);	btnRtl.setDisable(true);
		btnManual.setOnAction(btnManualEventHandler); btnManual.setDisable(true);	
		btnMissionMake.setOnAction(btnMissionMakeEventHandler); btnMissionMake.setDisable(true);
		btnMissionClear.setOnAction(btnMissionClearEventHandler); btnMissionClear.setDisable(true);
		btnMissionUpload.setOnAction(btnMissionUploadEventHandler); btnMissionUpload.setDisable(true);
		btnMissionDownload.setOnAction(btnMissionDownloadEventHandler); btnMissionDownload.setDisable(true);
		btnMissionStart.setOnAction(btnMissionStartEventHandler); btnMissionStart.setDisable(true);
		btnMissionStop.setOnAction(btnMissionStopEventHandler); btnMissionStop.setDisable(true);
		btnGetMissionFromFile.setOnAction(btnGetMissionFromFileEventHandler); btnGetMissionFromFile.setDisable(true);
		btnSaveMissionToFile.setOnAction(btnSaveMissionToFileEventHandler); btnSaveMissionToFile.setDisable(true);
		btnFenceMake.setOnAction(btnFenceMakeEventHandler); btnFenceMake.setDisable(true);
		btnFenceClear.setOnAction(btnFenceClearEventHandler); btnFenceClear.setDisable(true);
		btnFenceUpload.setOnAction(btnFenceUploadEventHandler); btnFenceUpload.setDisable(true);
		btnFenceDownload.setOnAction(btnFenceDownloadEventHandler); btnFenceDownload.setDisable(true);
		btnFenceEnable.setOnAction(btnFenceEnableEventHandler); btnFenceEnable.setDisable(true);
		btnFenceDisable.setOnAction(btnFenceDisableEventHandler); btnFenceDisable.setDisable(true);
		btnMessageView.setOnAction(btnMessageViewEventHandler);
		btnCameraView.setOnAction(btnCameraViewEventHandler);
		btnNorth.setOnAction(btnNorthEventHandler);
		btnSouth.setOnAction(btnSouthEventHandler);
		btnEast.setOnAction(btnEastEventHandler);
		btnWest.setOnAction(btnWestEventHandler);
		btnDroneSelect.setOnAction(btnDroneSelectHandler);
		//
		btnMissionReady.setOnAction(btnMissionReadyHandler);
		btnRootSet.setOnAction(btnRootSetHandler);
		btnPackage.setOnAction(btnPackageHandler);
		drone = new Drone();

		initHud();
		initMessageView();
		initCameraView();
		initFlightMap();

		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_HEARTBEAT,
				new MavJsonListener() {
					@Override
					public void receive(JSONObject jsonObject) {
						Platform.runLater(()->{
							btnConnect.setText("연결끊기");
							if(jsonObject.getBoolean("arm")) {
								btnArm.setText("시동끄기");
								btnTakeoff.setDisable(false);
								btnLand.setDisable(false);
								btnRtl.setDisable(false);
								btnManual.setDisable(false);
							} else {
								btnArm.setText("시동걸기");
								btnTakeoff.setDisable(true);
								btnLand.setDisable(true);
								btnRtl.setDisable(true);
								btnManual.setDisable(true);
								if(!drone.flightController.mode.equals(MavJsonMessage.MAVJSON_MODE_STABILIZE)) {
									drone.flightController.sendSetMode(MavJsonMessage.MAVJSON_MODE_STABILIZE);
								}
							}
						});
					}
				}
		);
	}
	//---------------------------------------------------------------------------------
	@FXML public StackPane hudPane;
	public Hud hud;
	public void initHud() {
		hud = new Hud();
		hudPane.getChildren().add(hud.ui);
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_HEARTBEAT,
        		new MavJsonListener() {
		            @Override
		            public void receive(JSONObject jsonMessage) {
		                hud.controller.setMode(jsonMessage.getString("mode"));
		                hud.controller.setArm(jsonMessage.getBoolean("arm"));
		            }
		        });
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_GLOBAL_POSITION_INT,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                    	hud.controller.setAlt(jsonMessage.getDouble("alt"));
                    }
                });
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_ATTITUDE,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
						double yaw = jsonMessage.getDouble("yaw");
						if(yaw < 0) {
							yaw += 360;
						}
                    	hud.controller.setRollPichYaw(
								jsonMessage.getDouble("roll"),
								jsonMessage.getDouble("pitch"),
								yaw
						);
                    }
                });
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_VFR_HUD,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                    	hud.controller.setSpeed(
                    			jsonMessage.getDouble("airSpeed"),
								jsonMessage.getDouble("groundSpeed"));
                    }
                });
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_SYS_STATUS,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                    	hud.controller.setBattery(
								jsonMessage.getDouble("voltageBattery"),
								jsonMessage.getDouble("currentBattery"),
								jsonMessage.getInt("batteryRemaining")
						);
                    }
                });
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_GPS_RAW_INT,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                    	hud.controller.setGpsFixed(jsonMessage.getString("fix_type"));
                    }
                });
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_STATUSTEXT,
                new MavJsonListener() {
                    private String text;
                    @Override
                    public void receive(JSONObject jsonMessage) {
                    	hud.controller.setStatusText(jsonMessage.getString("text"));
                    }
                });

		hud.controller.btnCamera.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(hud.controller.isVideoOn == false) {
					hud.controller.videoOn();
					drone.camera0.mqttListenerSet(new ImageListener() {
						@Override
						public void receive(byte[] image) {
						    hud.controller.videoImage(image);
						}
					});
				} else {
				    hud.controller.videoOff();
				    drone.camera0.mqttListenerSet(null);
				}
			}
		});
	}
	//---------------------------------------------------------------------------------
	@FXML public StackPane messageCamPane;
	public MessageView messageView;
	public void initMessageView() {
		messageView = new MessageView();
		messageCamPane.getChildren().add(messageView.ui);
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_RECEIVE_MESSAGE_ALL,
				new MavJsonListener() {
					@Override
					public void receive(JSONObject jsonMessage) {
						messageView.controller.addReceiveMessage(jsonMessage);
					}
				}
		);
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_SEND_MESSAGE_ALL,
				new MavJsonListener() {
					@Override
					public void receive(JSONObject jsonMessage) {
						messageView.controller.addSendMessage(jsonMessage);
					}
				}
		);
	}
	//---------------------------------------------------------------------------------
	public CameraView cameraView;
	public void initCameraView() {
		cameraView = new CameraView();
		messageCamPane.getChildren().add(cameraView.ui);
		cameraView.ui.setVisible(false);
		drone.camera1.mqttListenerSet(new ImageListener() {
			@Override
			public void receive(byte[] image) {
				cameraView.controller.videoImage(image);
			}
		});
	}
	//---------------------------------------------------------------------------------
	@FXML public BorderPane centerBorderPane;
	public FlightMap flightMap;
	public void initFlightMap() {
		flightMap = new FlightMap();
		flightMap.setApiKey("AIzaSyBR_keJURT-bAce2vHKIWKNQTC-GqJWRMI");
		centerBorderPane.setCenter(flightMap.ui);
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_HEARTBEAT,
        		new MavJsonListener() {
					@Override
					public void receive(JSONObject jsonMessage) {
						String mode = jsonMessage.getString("mode");
						flightMap.controller.setMode(mode);
						
						if(drone.flightController.homeLat == 0.0) {
                        	drone.flightController.sendGetHomePosition();
                        }
					}
				});
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_GLOBAL_POSITION_INT,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                    	flightMap.controller.setCurrLocation(
                    			jsonMessage.getDouble("currLat"), 
                    			jsonMessage.getDouble("currLng"), 
                    			jsonMessage.getDouble("heading"));
                    }
                });
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_HOME_POSITION,
        		new MavJsonListener() {
					@Override
					public void receive(JSONObject jsonMessage) {
						flightMap.controller.setHomePosition(
								jsonMessage.getDouble("homeLat"), 
								jsonMessage.getDouble("homeLng"));
						btnMissionMake.setDisable(false);
						btnMissionClear.setDisable(false);
						btnMissionUpload.setDisable(false);
						btnMissionDownload.setDisable(false);
						btnMissionStart.setDisable(false);
						btnMissionStop.setDisable(false);
						btnGetMissionFromFile.setDisable(false);
						btnSaveMissionToFile.setDisable(false);
						btnFenceMake.setDisable(false);
						btnFenceClear.setDisable(false);
						btnFenceUpload.setDisable(false);
						btnFenceDownload.setDisable(false);
						btnFenceEnable.setDisable(false);
						btnFenceDisable.setDisable(false);						
					}
				});
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_MISSION_ACK,
        		new MavJsonListener() {
					@Override
					public void receive(JSONObject jsonMessage) {
						flightMap.controller.showInfoLabel("미션 업로드 성공");
					}
				});		
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_MISSION_ITEMS,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                    	flightMap.controller.setMissionItems(jsonMessage.getJSONArray("items"));
                    	flightMap.controller.showInfoLabel("미션 다운로드 성공");
                    }
                });
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_MISSION_CURRENT,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                    	flightMap.controller.setMissionCurrent(jsonMessage.getInt("seq"));
                    }
                });
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_FENCE_ACK,
        		new MavJsonListener() {
					@Override
					public void receive(JSONObject jsonMessage) {
						flightMap.controller.showInfoLabel("펜스 업로드 성공");
					}
				});	
		
		drone.flightController.addMavJsonListener(
				MavJsonMessage.MAVJSON_MSG_ID_FENCE_POINTS,
                new MavJsonListener() {
                    @Override
                    public void receive(JSONObject jsonMessage) {
                    	flightMap.controller.fenceMapSync(jsonMessage.getJSONArray("points"));
                    }
                });
	}
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnMessageViewEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if(messageView != null) {
				messageView.ui.setVisible(true);
			}
			if(cameraView != null) {
				cameraView.ui.setVisible(false);
			}
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnCameraViewEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if(messageView != null) {
				messageView.ui.setVisible(false);
			}
			if(cameraView != null) {
				cameraView.ui.setVisible(true);
			}
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnConnectConfigEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			try {
				Stage dialog = new Stage();
				dialog.setTitle("Network Configuration");
				dialog.initModality(Modality.APPLICATION_MODAL);
				Scene scene = new Scene(NetworkConfig.getInstance().ui);
				scene.getStylesheets().add(GcsMain.class.getResource("style_dark_dialog.css").toExternalForm());
				dialog.setScene(scene);
				dialog.setResizable(false);
				dialog.show();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnConnectEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if(btnConnect.getText().equals("연결하기")) {
				drone.connect();
				// 드론과 연결되면 GCSMainMqtt 클리아언트 생성
				gcsMainMqtt = new GcsMainMqtt();
				System.out.println("GcsMainMqtt Created");
				// /drone/fc/pub 	subscribe
				fcMqttClient = new FCMqttClient();
				System.out.println("FCMqttClient Created Success");
			} else {
				drone.disconnect();
				btnConnect.setText("연결하기");
				btnArm.setText("시동걸기");
				new Thread(){
					@Override
					public void run(){
						try {
							fcMqttClient.client.disconnect();
							fcMqttClient.client.close();
							gcsMainMqtt.client.disconnect();
							gcsMainMqtt.client.close();
						} catch (MqttException e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnArmEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if (btnArm.getText().equals("시동걸기")) {
				drone.flightController.sendArm(true);
			} else {
				drone.flightController.sendArm(false);
			}
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnTakeoffEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			float alt = Float.parseFloat(txtTakeoffHeight.getText());
			drone.flightController.sendTakeoff(alt);
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnLandEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			//drone.flightController.sendSetMode("LAND");
			drone.flightController.sendSetMode(MavJsonMessage.MAVJSON_MODE_LAND);
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnRtlEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			//drone.flightController.sendSetMode("RTL");
			drone.flightController.sendSetMode(MavJsonMessage.MAVJSON_MODE_RTL);
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnManualEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			boolean isMove = chkManualMove.isSelected();
			boolean isAlt = chkManualAlt.isSelected();
			double manualAlt = Double.parseDouble(txtManualAlt.getText());

			if(isMove==false && isAlt==true) {
				drone.flightController.sendSetPositionTargetGlobalInt(
						drone.flightController.currLat,
						drone.flightController.currLng,
						manualAlt
				);
				return;
			}
			
			if(isMove == true) {
				flightMap.controller.mapListenerAdd("manualMove", new MapListener() {
					@Override
					public void receive(JSONObject jsonMessage) {
						drone.flightController.sendSetPositionTargetGlobalInt(
								jsonMessage.getDouble("targetLat"),
								jsonMessage.getDouble("targetLng"),
								jsonMessage.getDouble("targetAlt")
						);
					}
				});
				
				if(isAlt) {
					flightMap.controller.manualMake(manualAlt);
				} else {
					flightMap.controller.manualMake(drone.flightController.alt);
				}
			}
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnMissionMakeEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			flightMap.controller.missionMake();
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnMissionClearEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			flightMap.controller.missionClear();
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnMissionUploadEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if(currentMissionNumber == -1){
				GcsMain.instance.controller.flightMap.controller.showInfoLabel("미션이 선택되지 않았습니다.");
			}
			else if(droneNumber == -1){
				GcsMain.instance.controller.flightMap.controller.showInfoLabel("드론이 선택되지 않았습니다.");
			}
			else{
				JSONArray jsonArray = flightMap.controller.getMissionItems();
				if(jsonArray.length() < 2) {
					AlertDialog.showOkButton("알림", "미션 아이템 수가 부족합니다.");
				} else {
					drone.flightController.sendMissionUpload(jsonArray);
					new Thread(){
					}.start();
					fcMqttClient.SendMissionRoot();
					MissionUploadTrigger = true;
				}
			}
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnMissionDownloadEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			drone.flightController.sendMissionDownload();
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnMissionStartEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			drone.flightController.sendMissionStart();
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnMissionStopEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			//drone.flightController.sendSetMode("GUIDED");
			drone.flightController.sendSetMode(MavJsonMessage.MAVJSON_MODE_GUIDED);
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnGetMissionFromFileEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			flightMap.controller.readMissionFromFile();
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnSaveMissionToFileEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			flightMap.controller.writeMissionToFile();
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnFenceMakeEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			flightMap.controller.fenceMake();
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnFenceClearEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			flightMap.controller.fenceClear();
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnFenceUploadEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			flightMap.controller.mapListenerAdd("fencePoints", new MapListener() {
				@Override
				public void receive(JSONObject jsonMessage) {
					JSONArray jsonArray = jsonMessage.getJSONArray("points");
					if(jsonArray.length() < 4) {
						AlertDialog.showOkButton("알림", "펜스 포인트 수가 부족합니다.");
					} else {
						drone.flightController.sendFenceUpload(jsonArray);
					}
				}
			});
			
			flightMap.controller.getFencePoints();
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnFenceDownloadEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			drone.flightController.sendFenceDownload();
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnFenceEnableEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			drone.flightController.sendFenceEnable(true);
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnFenceDisableEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			drone.flightController.sendFenceEnable(false);
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnNorthEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			drone.flightController.sendFindControl(1, 0); //m/s
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnSouthEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			drone.flightController.sendFindControl(-1, 0); //m/s
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnEastEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			drone.flightController.sendFindControl(0, 1); //m/s
		}
	};
	//---------------------------------------------------------------------------------
	public EventHandler<ActionEvent> btnWestEventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			drone.flightController.sendFindControl(0, -1); //m/s
		}
	};

	public EventHandler<ActionEvent> btnMissionReadyHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			MissionRequesting missionRequesting = new MissionRequesting();
			missionRequesting.show();
		}
	};

	public EventHandler<ActionEvent> btnPackageHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			gcsMainMqtt.MagentActivate();
		}
	};

	public EventHandler<ActionEvent> btnRootSetHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			rootSetMethod();
		}
	};

	public EventHandler<ActionEvent> btnDroneSelectHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			ServiceDroneSelect serviceDroneSelect = new ServiceDroneSelect();
			serviceDroneSelect.show();
		}
	};

	public void pushX(){
		if(currentMissionNumber != -1){
			// 미션이 있는 상태에서 꺼진다면 갈제 RTL
			GcsMain.instance.controller.flightMap.controller.setMode("RTL");
		}
		new Thread(){
			@Override
			public void run() {
				gcsMainMqtt.resetDrone();
			}
		}.start();
	}

	private void ResetTriggers(){
		System.out.println("Reset All Status");
		this.FCMqttClientTrigger_GPS = false;
		this.FCMqttClientTrigger_Mission = false;
		this.WebMissionInTrigger = false;
		this.MissionUploadTrigger = false;
		this.DroneControllerTrigger = 0;
		this.gpsSendThread = 0;
		this.missionDone = 0;
		this.currentMissionNumber = -1;
		this.droneNumber = -1;
		this.missionCurrentSeqTrigger = 0;
	}

	public void setDestination(double getLat, double getLng, int missionNumber){
		this.destinationLat = getLat;
		this.destinationLng = getLng;
		this.currentMissionNumber = missionNumber;
	}

	public int getDroneNumber(){
		return this.droneNumber;
	}

	public int getReNumber(){
		return this.currentMissionNumber;
	}
	// 이벤트 핸들러 ----------------------------------------------------------------

	public void setCurrGps(String lat, String lng, String alt){
		this.currLat = lat;
		this.currLng = lng;
		this.currAlt = alt;
	}

	public String getCurrLat(){
		return this.currLat;
	}
	public String getCurrLng(){
		return this.currLng;
	}
	public String getCurrAlt(){
		return this.currAlt;
	}

	public void setDroneNumber(int d_number){
		this.droneNumber = d_number;
	}

	// FC 에서 Publish 하는 정보를 읽어낼 클라이언트
	public class FCMqttClient{
		private MqttClient client;

		private String gpsLat;
		private String gpsLng;

		public FCMqttClient(){
			while(true){
				try {
					client = new MqttClient("tcp://106.253.56.124:1881", MqttClient.generateClientId(), null);
					MqttConnectOptions options = new MqttConnectOptions();
					client.connect();
					break;
				}
				catch (MqttException e){
					System.out.println(e.getMessage());
				}
			}

			System.out.println("FCMqttClient MQTT Connected");

			client.setCallback(new MqttCallback() {
				@Override
				public void connectionLost(Throwable throwable) {
					System.out.println("FCMqttClient Connection Lost");
					reconnect();
				}

				@Override
				public void messageArrived(String s, MqttMessage mqttMessage){

					// Mission 을 드론에 업로드 하였다면 진행하도록

					if(MissionUploadTrigger){
						String msg = mqttMessage.toString();
						JSONObject jsonObject = new JSONObject(msg);
						String trigger = "HEARTBEAT";
						if(jsonObject.get("msgid").equals(trigger)){
							FCMqttClientTrigger_GPS = true; // GPS 보내는 트리거용
							FCMqttClientTrigger_Mission = true; // 이건 어디에 쓰는 지 흠
						}

						if(jsonObject.get("msgid").equals("GLOBAL_POSITION_INT")){
							// 실시간 현재 위치값 저장
							String getLat = String.valueOf(jsonObject.get("currLat"));
							String getLng = String.valueOf(jsonObject.get("currLng"));
							String getAlt = String.valueOf(jsonObject.get("alt"));
							GcsMain.instance.controller.setCurrGps(getLat, getLng , getAlt);
						}

						if(jsonObject.get("msgid").equals("MISSION_CURRENT")){
							int missionSize =  GcsMain.instance.controller.flightMap.controller.getMissionItems().length();
							System.out.println("MissionSize : " + missionSize);
							int missionCurrentSeq = (int) jsonObject.get("seq");
							System.out.println("MissionCurrent : " + missionCurrentSeq);
							if(missionCurrentSeqTrigger == 1){
								// 목적지 도달 이후 출발지로 디시 돌아왔음을 보여줄 수 있도록
								if(missionCurrentSeq == 0){
									// 목적지 도달 이후 정상적인 RTL가 되었으면 완전한 미션 종료임으로
									// 이를 웹에 전달한다.
									missionCurrentSeqTrigger = 2;
									new Thread(){
										@Override
										public void run() {
											fcMqttClient.SendMissionEndToWeb();
											System.out.println("reset All Status Trigger");
											GcsMain.instance.controller.ResetTriggers();
											// ResetTriggers();
											System.out.println("done reset All Status Trigger");
										}
									}.start();
								}
							}
							if(missionCurrentSeq == missionSize / 2 + 1){
								// 해당 공식은 미션이 수행 중에 있어 목적지에 도착한 이후 Delay 다음 시퀀스의 미션을 수행할 때 작동된다.
								missionCurrentSeqTrigger = 1;
								// missionCurrentSeq 가 0이 아닐 것 이니까.
								if(DroneControllerTrigger == 0){
									System.out.println("DroneControllerTrigger 0 to 1");
									DroneControllerTrigger = 1;
									// 미션이 도착지에 완료했음에 대한 메세지를 Web 과 Android 에 전송
									new Thread(){
										@Override
										public void run(){
											System.out.println("try Publish MissionEnd");
											fcMqttClient.SendMissionEndToAndroid();
											System.out.println("done Publish MissionEnd");
										}
									}.start();
									// 이게 수행되기 전에 한번 더 돌아서 service04가 두번 실행되는 경우가 있음
									//
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											try {
												System.out.println("Service04 Activated");
												ServiceDialog04 serviceDialog04 = new ServiceDialog04();
												serviceDialog04.show();
												System.out.println("Service04 Successfully Done");
											}
											catch (Exception e){
												e.printStackTrace();
											}
										}
									});
									// drone 상태를 GUIDED로 변경하기
									drone.flightController.sendSetMode(MavJsonMessage.MAVJSON_MODE_GUIDED);
									// 목적지 까지 한번 왔다 갔나 확인용
									missionDone = 1;
									System.out.println("MissionDone 0 to 1");
								}
							}
						}

						// 너무 자주 GPS 정보를 보내지 않도록
						if(FCMqttClientTrigger_GPS){
							if(jsonObject.get("msgid").equals("GLOBAL_POSITION_INT")) {
								System.out.println("--------------");
								gpsLat = String.valueOf(jsonObject.get("currLat"));
								gpsLng = String.valueOf(jsonObject.get("currLng"));

								JSONObject object = new JSONObject();
								object.put("msgid", "droneGps");
								object.put("lat", gpsLat);
								object.put("lng", gpsLng);
								object.put("missionNumber", currentMissionNumber);
								// 목적지 까지 간 다음에는 굳이 다시 보낼 필요는 없으니까
								if(missionDone == 1){
									System.out.println("when missionDone is 1. than clear the marker");
									GcsMain.instance.controller.flightMap.controller.requestMarkClear();
									// 목적지 Marker 삭제
									missionDone = 2;
									// 미션 종료에 따른 트리거
								}
								// 안드로이드에 드론 좌표 전송 용
								// 아직 목적지 seq에 도착하지 않았다면 수행토록
								else if(missionDone == 0){
									//  System.out.println("missionDone : " + missionDone);
									if (gpsSendThread == 0) {
										try {
											new Thread() {
												@Override
												public void run() {
													try {
														System.out.println("GPS Publish Try");
														client.publish("/android/page2", object.toString().getBytes(), 0, false);
														System.out.println("GPS Publish Done");
														gpsSendThread = 1;
														Thread.sleep(1000);
														gpsSendThread = 0;
													} catch (InterruptedException e) {
														e.printStackTrace();
													} catch (MqttPersistenceException e) {
														e.printStackTrace();
													} catch (MqttException e) {
														e.printStackTrace();
													}
												}
											}.start();
										} catch (Exception e) {
											System.out.println("GPS Fail");
											System.out.println(e.getMessage());
										}
									}
								}
								// 한번 보냈음을 완료함을 알려줌
								FCMqttClientTrigger_GPS = false;
							}
						}
					}
				}
				@Override
				public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

				}
			});
			// SubScribe 설정
			try {
				client.subscribe("/drone/fc/pub");
				System.out.println("FCMqttClient subscribe done");
			} catch (MqttException e) {
				System.out.println("FCMqttClient subscribe fail");
				e.printStackTrace();
			}
		}

		public void reconnect(){
			while(true){
				try {
					client = new MqttClient("tcp://106.253.56.124:1881", MqttClient.generateClientId(), null);
					MqttConnectOptions options = new MqttConnectOptions();
					client.connect();
					break;
				}
				catch (MqttException e){
					System.out.println(e.getMessage());
				}
			}
		}

		// Web 에는 미션 경로를 보내주고
		// android 에는 mission 이 시작되었다고 알려 줌
		public void SendMissionRoot(){

			JSONObject settingRoot = new JSONObject();
			JSONArray totalRoot = GcsMain.instance.controller.flightMap.controller.getMissionItems();
			JSONArray spotRoot = new JSONArray(); // web에 보낼 거. 왕복
			JSONArray mobileRoot = new JSONArray(); // mobile에 보낼 거. 편도
			int seqStatus = 0;

			for(int i = 0 ; i < totalRoot.length() ; i++){
				JSONObject jsonObject = (JSONObject) totalRoot.get(i);
				int getCommand = jsonObject.getInt("command");
				if(getCommand == 16){ // command 16은 이동 명령 중
					spotRoot.put(jsonObject); // 기록이니 다넣고

					if((int)jsonObject.get("seq") == 0){
						mobileRoot.put(jsonObject); // 모바일 용은 0번째와
					}
					else if((int)jsonObject.get("seq") - 1 == seqStatus){
						seqStatus = (int)jsonObject.get("seq"); // 이후 delay 이전까지 모든 내용을 저장
						mobileRoot.put(jsonObject);
					}

					// 이동 명령 spot 만 전송할 것이다.
				}
			}
			settingRoot.put("msgid","missionSpots");
			settingRoot.put("missionSpots", mobileRoot);
			//settingRoot.put("missionSpots",spotRoot);
			settingRoot.put("missionNumber", currentMissionNumber);
			settingRoot.put("droneNumber", droneNumber);
			try{
				System.out.println("Trying MissionSpots Publish");
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("msgid", "missionStatus");
				jsonObject.put("status", "missionStart");
				jsonObject.put("missionNumber",currentMissionNumber);
				jsonObject.put("missionSpots", mobileRoot.toString()); // mobile은 toString해서 보내달라 했으니 toString 해서
				System.out.println("missionNumber : " + currentMissionNumber);
				client.publish("/android/page1", jsonObject.toString().getBytes(), 0, false);
				//client.publish("/web/missionStatus", jsonObject.toString().getBytes(), 0, false);
				client.publish("/web/missionStatus", settingRoot.toString().getBytes(), 0, false);
				System.out.println("Done  MissionSpots Publish");
			}
			catch (MqttException e){
				e.printStackTrace();
			}

		}

		// 미션이 종료가 되었다고 알려줄 메소드
		public void SendMissionEndToAndroid(){

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("msgid", "missionStatus");
			jsonObject.put("status", "missionFinish");
			jsonObject.put("missionNumber", currentMissionNumber);
			jsonObject.put("droneNumber" , droneNumber);
			System.out.println("MissionEnd Publish data : " + jsonObject.toString());
			try{
				System.out.println("Trying MissionEnd To Mobile Publish");
				client.publish("/android/page2", jsonObject.toString().getBytes(), 0, false);
				System.out.println("Done MissionEnd To Mobile Publish");
			}
			catch (MqttException e){
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}

		public void SendMissionEndToWeb(){

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("msgid", "missionStatus");
			jsonObject.put("status", "missionFinish");
			jsonObject.put("missionNumber", currentMissionNumber);
			jsonObject.put("droneNumber" , droneNumber);
			System.out.println("MissionEnd Publish data : " + jsonObject.toString());
			try{
				System.out.println("Trying MissionEnd To Web Publish");
				client.publish("/web/missionStatus", jsonObject.toString().getBytes(), 0, false);
				System.out.println("Done MissionEnd To Web Publish");
			}
			catch (MqttException e){
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	// Android 와 Web 에서 전송하는 데이터를 받을 클라이언트
	public class GcsMainMqtt{

		private MqttClient client;

		public GcsMainMqtt(){
			while(true){
				try {
					client = new MqttClient("tcp://106.253.56.124:1881", MqttClient.generateClientId(), null);
					MqttConnectOptions options = new MqttConnectOptions();
					client.connect();
					System.out.println("GcsMainMqtt client Connect Done");
					break;
				}
				catch (MqttException e){
					System.out.println("GcsMainMqtt client Connect Error");
					System.out.println(e.getMessage());
				}
			}
			make_sub();
		}

		public void make_sub(){
			client.setCallback(new MqttCallback() {
				@Override
				public void connectionLost(Throwable throwable) {
					System.out.println("GcsMainMqtt Connection Lost");
				}

				@Override
				public void messageArrived(String s, MqttMessage mqttMessage){

					String strmsg = new String(mqttMessage.getPayload());
					System.out.println("Message Arrived /gcs/main : " + strmsg);
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

				}
			});
			try {
				client.subscribe("/gcs/main");
				System.out.println("GCSMainMqtt client Subscribe Success");
			} catch (MqttException e) {
				System.out.println("GCSMainMqtt client Subscribe Fail");
				e.printStackTrace();
			}
		}

		public void MagentActivate(){

			System.out.println("Magnet Off Control");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("magnet", "on");
			try {
				System.out.println("Trying Magnet Activate Publish");
				client.publish("/drone/magnet/sub", jsonObject.toString().getBytes(), 0, false);
				System.out.println("Done Magnet Activate Publish");
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}


		public void resetDrone(){

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("msgid", "droneReset");
			jsonObject.put("droneNumber", GcsMain.instance.controller.getDroneNumber());
			jsonObject.put("missionNumber" , currentMissionNumber);
			try {
				System.out.println("resetDrone Try");
				client.publish("/web/missionStatus" , jsonObject.toString().getBytes(), 0 , false);
				System.out.println("resetDrone Done");
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}

	public void rootSetMethod(){
		// 현재 미션 아이템을 가져와 여기에 마지막 부분에 미션 아이템을 추가한다.
		JSONArray array = GcsMain.instance.controller.flightMap.controller.getMissionItems();
		int arrSize = array.length();
		// 목적지 seq 저장
		JSONObject destiObject = new JSONObject();
		destiObject.put("seq", arrSize);
		destiObject.put("command",16);
		destiObject.put("x", destinationLat);
		destiObject.put("y", destinationLng);
		destiObject.put("z", 10);
		destiObject.put("param1", 0);
		destiObject.put("param2", 0);
		destiObject.put("param3", 0);
		destiObject.put("param4", 0);
		System.out.println("destiObject : " + destiObject.toString());
		array.put(destiObject);
		System.out.println("---------");

		// 목적지 도착 후 delay를 주기위함
		JSONObject delayObject = new JSONObject();
		delayObject.put("seq", arrSize + 1);
		delayObject.put("command",93);
		delayObject.put("x",destinationLat);
		delayObject.put("y",destinationLat);
		delayObject.put("z",10);
		delayObject.put("param1", 10); // 딜레이 되는 시간
		delayObject.put("param2", 0);
		delayObject.put("param3", 0);
		delayObject.put("param4", 0);
		System.out.println("delayObject : " + delayObject);
		array.put(delayObject);
		System.out.println("--------------");

		// root 목적지 제외하여 변경토록 함
		JSONArray getAdd = GcsMain.instance.controller.flightMap.controller.getMissionItems();
		System.out.println("Will add");
		int idx = array.length(); // 추가되기 전의 인덱스를 가져와 seq 변경
		// int i = getAdd.length() - 1 추가되기 전의 온전한 리스트를 가져와(경로만있는)
		// 이를 적용한다. 즉 경로만 지정된 것에서 마지막 부분을 제외하고 seq를 다시 set하고
		// array에 추가한다.
		for(int i = getAdd.length() - 1 ; i > 0 ; i--){
			JSONObject object = (JSONObject) getAdd.get(i);
			object.put("seq", idx); // 변경된 array
			System.out.println(object.toString());
			idx++;
			array.put(object);
		}

		// 마지막 root를 지우고 이를 RTL로 변경
		JSONObject RTL = new JSONObject();
		RTL.put("seq", array.length());
		RTL.put("x", destinationLat);
		RTL.put("y", destinationLng);
		RTL.put("z", 0);
		RTL.put("command", 20);
		RTL.put("param1", 0);
		RTL.put("param2", 0);
		RTL.put("param3", 0);
		RTL.put("param4", 0);
		System.out.println("Last RTL : " + RTL.toString());
		array.put(RTL);

		// 이륙 추가


		GcsMain.instance.controller.flightMap.controller.missionClear();
		// 설계한 미션들 삭제
		//GcsMain.instance.controller.flightMap.controller.missionMake();
		GcsMain.instance.controller.flightMap.controller.setMissionItems_Customize(array);
	}



}
