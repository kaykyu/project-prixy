import { CanActivateChildFn, CanActivateFn, Router } from "@angular/router";
import { inject } from "@angular/core";
import { User } from "./models";
import { AuthService } from "./service/auth.service";

export const isTokenValid: CanActivateFn =
    async (route, _state) => {
        const router = inject(Router)
        const token: string = route.params['token']
        const user: User = JSON.parse(atob(token))
        if (user.exp < Date.now() / 1000)
            return router.parseUrl('/error?error=expired');
        else if (user.iss !== 'Prixy')
            return router.parseUrl('/error?error=invalidlink')
        return true;
    }

export const checkAuth: CanActivateChildFn =
    async (route, state) => {
        const router = inject(Router)
        const authSvc = inject(AuthService)
        return authSvc.checkAuth()
            .then(value => {
                if (state.url.endsWith('kitchen')) {
                    route.data = { role: value.role }
                    return true
                } else if (value.role !== 'CLIENT') {
                    alert('Kitchen account has limited access')
                    return router.parseUrl('/main/kitchen')
                }
                return true
            })
            .catch(() => router.parseUrl('/login'))
    }

export const isLoggedIn: CanActivateFn =
    async (_route, _state) => {
        const router = inject(Router)
        const authSvc = inject(AuthService)
        return authSvc.checkAuth()
            .then(() => router.parseUrl('/main'))
            .catch(() => true)
    }
