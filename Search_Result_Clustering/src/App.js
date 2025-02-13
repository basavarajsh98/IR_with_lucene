import "./App.css";
import { useState } from "react";
import axios from "axios";
import Highcharts from "highcharts";

import HighchartsReact from "highcharts-react-official";

require("highcharts/modules/exporting")(Highcharts);
require("highcharts/highcharts-more")(Highcharts);

function App() {
  const [value, setValue] = useState("");
  const [data, setData] = useState([]);

  if (data) {
    var highchart_data = [];
    for (var key in data) {
      highchart_data = highchart_data.concat({
        name: 'Cluster '+ parseInt(key),
        data: data[key].map((doc) => {
          return { name: doc.title, value: doc.score };
        }),
      });
    }
  }

  const changeHandler = (e) => {
    API(value);
  };

  const API = (query) => {
    console.log(query);
    var params = {
      id: query,
    };

    if (query !== "NONE") {
      axios.post("/query", params).then(function (response) {
        setData(response.data);
      });
    }
  };

  const chartOptions = {
    chart: {
      type: "packedbubble",
      height: "100%",
    },
    title: {
      text: "Cluster Results",
    },
    tooltip: {
      useHTML: true,
      pointFormat: "<b>Name:{point.name}:</b><br> Score:{point.value}",
    },
    plotOptions: {
      packedbubble: {
        minSize: "30%",
        maxSize: "80%",
        zMin: 0,
        zMax: 1000,
        layoutAlgorithm: {
          gravitationalConstant: 0.02,
          splitSeries: true,
          seriesInteraction: false,
          dragBetweenSeries: true,
          parentNodeLimit: true,
        },
        dataLabels: {
          enabled: true,
          format: "{series.name}",
          filter: {
            property: "y",
            operator: ">",
            value: 250,
          },
          style: {
            color: "black",
            textOutline: "none",
            fontWeight: "normal",
          },
        },
      },
    },
    series: highchart_data,
  };

  return (
    <div className="App">
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
        <div className="text-center">
          <a className="navbar-brand" href="/">
            Information Retrieval
          </a>
        </div>
        <div className="container-fluid">
          <div className="collapse navbar-collapse" id="navbarSupportedContent">
            <ul className="navbar-nav mr-auto mb-2 mb-lg-0">
              <li className="nav-item">
                <a className="nav-link active" aria-current="page" href="/">
                  Home
                </a>
              </li>
            </ul>
          </div>
        </div>
      </nav>
      <div className="container">
        <div className="row height d-flex justify-content-center">
          <div className="col-md-8">
            <div className="search">
              <i className="fa fa-search"></i>
              <input
                type="search"
                className="form-control"
                placeholder="Search Here.."
                value={value}
                onChange={(e) => {
                  setValue(e.target.value);
                }}
              />
              <button className="btn btn-primary" onClick={changeHandler}>
                Search
              </button>
            </div>
          </div>
        </div>
        <HighchartsReact
          highcharts={Highcharts}
          options={chartOptions}
        ></HighchartsReact>
      </div>
    </div>
  );
}

export default App;
