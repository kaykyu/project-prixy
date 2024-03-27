import { Component, OnInit } from '@angular/core';
import { ClientService } from '../../service/client.service';
import { Chart, registerables } from 'chart.js';
import { Stats } from '../../models';

@Component({
  selector: 'app-client-dashboard',
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.css'
})
export class ClientDashboardComponent implements OnInit {

  constructor(private clientSvc: ClientService) {
    Chart.register(...registerables)
  }

  default: number = 30
  options = options
  data!: boolean
  stats!: Stats
  chart!: Chart

  ngOnInit(): void {
    this.getStats()
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
      .catch(() => this.data = false)
  }

  processHourlyCount(): number[] {
    var result = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    if (!!this.stats.hourly)
      this.stats.hourly.forEach(value => {
        result[value._id] = value.count
      })
    return result
  }

  createChart() {
    this.chart = new Chart('canvas', {
      type: 'bar',
      data: {
        labels: hours,
        datasets: [
          {
            label: 'No. of orders',
            data: this.processHourlyCount(),
            borderWidth: 0
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