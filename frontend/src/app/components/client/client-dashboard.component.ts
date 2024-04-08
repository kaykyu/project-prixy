import { Component, Input, OnInit } from '@angular/core';
import { ClientService } from '../../service/client.service';
import { Chart, registerables } from 'chart.js';
import { Client, Stats } from '../../models';

@Component({
  selector: 'app-client-dashboard',
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.css'
})
export class ClientDashboardComponent implements OnInit {

  constructor(private clientSvc: ClientService) {
    Chart.register(...registerables)
  }

  @Input() client!: Client
  default: number = 30
  options = options
  data!: boolean
  stats!: Stats
  chart!: Chart

  ngOnInit(): void {
    this.getStats()
  }

  download() {
    this.clientSvc.getRecords(this.default)
      .then(value => {
        const a = document.createElement("a");
        a.href = "data:text/csv," + value.csv;
        a.setAttribute('download', `${this.client.estName}_Records_${this.stats.first}_to_${this.stats.last}.csv`.replace(/\s+/g,'_'));
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      })
  }

  async getStats(): Promise<any> {
    if (this.chart)
      this.chart.destroy()

    return this.clientSvc.getStats(this.default)
      .then(value => {
        this.data = true
        this.stats = value
      })
      .then(() => this.createChart())
      .catch(() => {
        this.stats = { sales: 0, top: [], hourly: [], first: '', last: '' }
        this.data = false
      })
  }

  processHourlyCount(): number[] {
    var result = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    if (!!this.stats.hourly)
      this.stats.hourly.forEach(value => {
        result[value._id] = value.count
      })
    return result
  }

  createChart() {
    this.chart = new Chart('canvas', {
      type: 'line',
      data: {
        labels: hours,
        datasets: [
          {
            label: 'No. of orders',
            data: this.processHourlyCount(),
            fill: false,
            borderColor: '#8585ff',
            tension: 0.1
          }
        ]
      },
      options: {
        backgroundColor: '#8585ff',
        scales: {
          y: {
            beginAtZero: true
          },
          x: {
            title: {
              display: true,
              text: 'Time, 24-hour'
            }
          }
        },
        plugins: {
          tooltip: {
            callbacks: {
              title(tooltipItems) {
                return tooltipItems.map(value => value.label + ':00H')
              }
            }
          }
        }
      }
    })
  }

}

const options = [
  { value: 365, label: 'Year' },
  { value: 30, label: 'Month' },
  { value: 14, label: 'Fortnight' },
  { value: 7, label: 'Week' }
]

const hours = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23]