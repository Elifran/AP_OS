# DIAGNOSTIQUE DES MACHINES - APP_OS project
## Author Information

- **Name:** RATOVOARIMANANA Maminiaina Fidèle
- **Alias:** El'Ifran
- **University:** University Mohammed V of Rabat
- **School:** The National School of Arts and Crafts
- **Field of Study:** Electric Power Digital Industry
- **Department:** Electrical Engineering

    
## Description

This project focuses on analyzing the vibrational signals emitted by rotating machines to gain insights into their health conditions. 
The analysis is rooted in detecting the vibrational patterns associated with faults that occur when machines experience malfunctions.

## Table of Contents

1. [Installation](#Installation)
2. [Usage](#usage)
3. [Contributing](#contributing)
4. [License](#license)

## Intruction of use
### Get file.ak
1. go to the link bellow, and dowload the file .apk

```
https://github.com/Elifran/AP_OS/blob/master/app/release/app-release.apk
```
or dowload it directly from here : [Download APK file](https://github.com/Elifran/AP_OS/raw/master/app/release/app-release.apk)

2. Install the application in your mobile phone, make sur tha your phone accept the installation from your internal memory
3. Get started.

### Use the application
Once you install the application, you will be in the main screen. and normally, your phone begin the calibration on your accelerometer sensor.
NB : some authorisation must be required, like fast hardware reuqest for sensor.

#### Set configuration
Before doing whatether, configure the app first, to do that, follow the instruction bellow
1. Wait :
   if the aff is oppened, wait until the pup-pop mesage `filter ...`appear.
2. Go to setting
   after the pup+pop message appeard, click the round button in the top-left of the screen to get the menu

3. Type your configuration like the following description

    - Resolution `0 - ...` : This is how the output signal can or can't have a good differentiation between two signals.
    - Line frequency `default 50.0Hz` : This defines your power line frequency for the alternating line. If it's a DC motor, this is useless.
    - Switch selector : Select the fault that needs to be identified on the machine. There are 6 options that you can select (all, some, or none of them).
    - Machine power : The power of the machine, defined in kW.
    - Power Coefficient : Through the power machine, it will define a constant power machine over power vibration.
    - Machine RPM : The velocity of the machine in rotations per minute. This can be written as an approximate value, then can be tracked by clicking on the `track` button.
    - Bearing Specifications
      - Ball Number : The ball number refers to the quantity of balls contained within the bearing. It plays a critical role in determining the load-carrying capacity and performance characteristics of the bearing.
      - Bearing Ball Diameter : The bearing ball diameter indicates the size of the individual balls within the bearing. This dimension significantly influences factors such as load capacity, stiffness, and rotational speed capability of the bearing.
      - Pitch Diameter : The pitch diameter of a bearing is a theoretical dimension used for calculating the geometry of bearing components. It represents the diameter where the contact between the balls and the races occurs.
      - Bearing Angle Presentation : The bearing angle presentation refers to the angle at which the bearing balls make contact with the races. This angle affects the bearing's load capacity, stiffness, and operational behavior.
    > **Note:** Please refer to specific documentation or technical specifications for detailed information on these bearing parameters in the context of your machine.
    - Noise Coefficient : Refers to the noise/blank noise present in your output (can be seen in the graph).
    - Analyse performances variables
      - Coefficient `16 - 256` : Refers to the number of points for the calculation of the statistics to get the frequency element. The bigger this value, the better the statistics, but it will decrease the power detection (variance - Average).
      - Threshold coefficient `AVG 8` : The value that the signal needs to have to identify as a peak element.
      - Influence `0 - 1`: Element to be set to determine the influence of the actual signal on the previous signal. If it's 0, the actual signal has no influence on the previous signal, and if it's 1, the actual signal has full influence on the previous.
    - **CONFIRM BUTTON** : Save all the configurations set.

4. Get the information
   After saving the setting, push the **BEGIN** button to see the time and frequency representation of the signal from the sensor. In this state, you can define manually what are the defaults in the machine when you see the frequency signal. If the signal is truncated or stopped in the middle of the screen, close the app and reopen it, and wait until the filter is initialized and re-push the **BEGIN** button.
   If all ok, to do the automatic diagnostic, fix the phone in the machine and wait until the signal to get established.
   - Adjust RPM: Even if the velocity is set before, make sure that it's the correct value. To ensure that, go to the menu again, and push the **TRACK** button, and select the real velocity from the button (the signal plot must be in progress).
   - Adjust resolution: Based on the signal plot on the screen, if it's not good enough, change the value of the *resolution*.
   > **Note:** Increasing this value increases the time of the screen by 2, and the time of collecting data by 8.

5. Do the analysis automatically
   All previous steps lead to this final step.
   - If the mobile phone is fixed into the machine, wait for the signal to establish (all config must be ok).
   - Push the button **READY TO COLLECT** or **RE-DO-ANALYZE** and wait until it says **READY ANALYZE**.
     > **Note:** Don't touch the phone during collecting data to avoid error vibration.
   - If the button changes to **READY ANALYZE**, push it and see the result.
     > **Note:** If you do not have any frequency peaks in the result, but you have seen them on the display, please double-check or ajust your configuration settings to ensure they are correctly set up for peak detection, adjust the *Analyse Performances Variables*.
> **RESULT :** Actually, the result must appear, and the defaults must be identified.

   
## Get source file and ///
### Installation

To get and set up this project, follow these steps:

1. Clone the Repository: Open your terminal or command prompt and run the following command to clone the repository onto your local machine:

    ```
    git clone https://github.com/Elifran/AP_OS
    ```
    

2. Open the Project in Android Studio: After cloning the repository, navigate to the directory where it was cloned (`AP_OS`), then open Android Studio. Select `Open an Existing Project`, navigate to the `AP_OS` directory, and select it. Android Studio will then open the project.

3. Install Dependencies: Once the project is opened in Android Studio, it will automatically handle dependency installation during project sync. You may see a progress bar indicating Gradle is syncing the project and downloading dependencies. 

4. Build and Run: After the project sync is complete, you can build and run the project on your Android device or emulator.

### Contributing

Contributions are welcome! If you would like to contribute to this project, please follow these guidelines:

1. Fork the repository.
2. Create a new branch for your feature or bug fix: `git checkout -b feature/my-feature`.
3. Make your changes and commit them: `git commit -am 'Add new feature'`.
4. Push to the branch: `git push origin feature/my-feature`.
5. Submit a pull request.

## License

This project is licensed under the [No License](LICENSE).
