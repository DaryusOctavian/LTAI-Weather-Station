//
//  ContentView.swift
//  API Test
//
//  Created by Darius Chifor Octavian on 5/8/23.
//

import SwiftUI
import Charts

struct Sensor {
    var tempValues     : [Data] = [];
    var humidValues    : [Data] = [];
    var windValues     : [Data] = [];
    var pressureValues : [Data] = [];
    
    struct SensorData : Codable {
        public class SensorDataValue : Codable {
            let time     : String
            let value    : Double
            let dataType : String
        }
        
        let temp     : SensorDataValue
        let humidity : SensorDataValue
        let pressure : SensorDataValue
        let wind     : SensorDataValue
    }
    
    struct Data : Identifiable {
        let id        = UUID()
        let date      : Date
        let value     : Double
    }
    
    enum LocalError : Error {
        case runtimeError(String)
    }
    
    public init() async throws {
        let data = try! await getSensorHistory()
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'"
        
        for sensorData in data {
            let toAppend = Data(
                date: dateFormatter.date(from: sensorData.time).unsafelyUnwrapped,
                value: sensorData.value
            )
            switch sensorData.dataType{
            case "temp":
                self.tempValues.append(toAppend)
            case "humidity":
                self.humidValues.append(toAppend)
            case "wind":
                self.windValues.append(toAppend)
            case "pressure":
                self.pressureValues.append(toAppend)
            default:
                throw LocalError.runtimeError("Wrong data in JSON :(")
            }
        }
    }
    
    public func getSensorHistory() async throws -> [SensorData.SensorDataValue] {
        guard let url: URL = URL(string: "https://awu4j6hku3.execute-api.eu-central-1.amazonaws.com/dev/weather/hist?days=1&group=24") else {
            throw LocalError.runtimeError("URL GET Failed :(")
        }
        
        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            
            let decoded: [SensorData.SensorDataValue] = try JSONDecoder().decode([SensorData.SensorDataValue].self, from: data)
            
            return decoded
        } catch {
            throw LocalError.runtimeError("Failed to decode URL to JSON Array :(")
        }
    }
    
    public func getSensorData() async throws -> SensorData {
        guard let url: URL = URL(string: "https://awu4j6hku3.execute-api.eu-central-1.amazonaws.com/dev/weather/latest") else {
            throw LocalError.runtimeError("URL GET Failed :(")
        }
        
        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            
            let decoded: SensorData = try JSONDecoder().decode(SensorData.self, from: data)
            
            return decoded
        } catch {
            throw LocalError.runtimeError("Failed to decode URL to JSON :(")
        }
    }
    
    public func getTemperatureValues() -> [Data] {
        return tempValues
    }
    
    public func getHumidityValues() -> [Data] {
        return humidValues
    }
    
    public func getWindValues() -> [Data] {
        return windValues
    }
    
    public func getPressureValues() -> [Data] {
        return pressureValues
    }
}

struct LineChartView : View {
    let data      : [Sensor.Data]
    let chartName : String
    
    var body : some View {
        VStack {
            GroupBox {
                Text(chartName)
                    .bold()
                Chart {
                    ForEach(data) {
                        LineMark(
                            x: .value("Time", $0.date, unit: .hour),
                            y: .value(chartName, $0.value)
                        )
                    }
                }
            }
        }
    }
}

public struct ContentView : View {
    @State private var temperatureValues : [Sensor.Data] = []
    @State private var humidityValues    : [Sensor.Data] = []
    @State private var windValues        : [Sensor.Data] = []
    @State private var pressureValues    : [Sensor.Data] = []

    public var body: some View {
        VStack(alignment: .leading) {
//            Text("Temperature").bold()
//                .foregroundColor(.red)
//
//            BarChartView(data: temperatureValues, colors: [.red, .orange])
            LineChartView(data: temperatureValues, chartName: "Temperature")
                .foregroundColor(.red)
            
//            Text("Humidity").bold()
//                .foregroundColor(.blue)
//
//            BarChartView(data: humidityValues, colors: [.blue, .purple])
            LineChartView(data: humidityValues, chartName: "Humidity")
                .foregroundColor(.blue)
            
//            Text("Wind").bold()
//                .foregroundColor(.green)
//
//            BarChartView(data: windValues, colors: [.green, .yellow])
            LineChartView(data: windValues, chartName: "Wind")
                .foregroundColor(.green)
            
            LineChartView(data: pressureValues, chartName: "Pressure")
                .foregroundColor(.yellow)
        }
        .padding()
        .task {
            let Sensors = try! await Sensor()
            temperatureValues = Sensors.getTemperatureValues()
            humidityValues = Sensors.getHumidityValues()
            windValues = Sensors.getWindValues()
            pressureValues = Sensors.getPressureValues()
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

/*
 struct BarView : View {
     var datum  : Double
     var colors : [Color]

     var gradient: LinearGradient {
         LinearGradient(gradient: Gradient(colors: colors), startPoint: .top, endPoint: .bottom)
     }

     var body: some View {
       Rectangle()
         .fill(gradient)
         .opacity(datum == 0.0 ? 0.0 : 1.0)
     }
 }

 struct BarChartView : View {
     var data   : [Sensor.Data]
     var colors : [Color]

     var highestData: Double {
         var mx = 0.0
         for e in data {
             mx = max(mx, e.value)
         }
         if mx == 0 { return 1.0 }
         return mx
     }

     var body: some View {
         GeometryReader {
             geometry in HStack(alignment: .bottom, spacing: 4.0) {
                 ForEach(data.indices, id: \.self) {
                     index in let width = (geometry.size.width / CGFloat(data.count)) - 4.0
                     let height = geometry.size.height * data[index].value / highestData

                     BarView(datum: data[index].value, colors: colors)
                         .frame(width: width, height: height, alignment: .bottom)
                 }
             }
         }
     }
 }

 */

/*

struct ContentView: View {
    var body: some View {
        VStack {
            Button("Proba") {}
                .padding(.all)
        }
        .padding()
    }
}
 
*/

//    static func mockData(_ count : Int, in range: ClosedRange<Double>) -> [Double] {
//        (0..<count).map { _ in .random(in: range) }
//    }
