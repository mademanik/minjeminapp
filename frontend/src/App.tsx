import "./App.css";
import keycloak from "./keycloak";
import {ReactKeycloakProvider, useKeycloak} from "@react-keycloak/web";
import {BrowserRouter as Router, Navigate, Route, Routes} from "react-router-dom";

// Dashboard Pages
import Dashboard from "./pages/Dashboard";
import Products from "./pages/Products";
import Rentals from "./pages/Rentals";
import Home from "./pages/Home.tsx";
import {Spin} from "antd";
import AdminRoute from "./components/AdminRoute.tsx";
import RequestRentals from "./pages/RequestRentals.tsx";

function App() {
    return (
        <ReactKeycloakProvider authClient={keycloak}>
            <Router>
                <SecuredContent/>
            </Router>
        </ReactKeycloakProvider>
    );
}

const SecuredContent: React.FC = () => {
    const {keycloak, initialized} = useKeycloak();

    if (!initialized) {
        return (
            <div style={{display: "flex", justifyContent: "center", marginTop: "20%"}}>
                <Spin size="large" tip="Initializing authentication..."/>
            </div>
        );
    }

    const isLoggedIn = keycloak.authenticated;

    if (!isLoggedIn) {
        keycloak.login();
        return (
            <div style={{display: "flex", justifyContent: "center", marginTop: "20%"}}>
                <Spin size="large" tip="Redirecting to login..."/>
            </div>
        );
    }

    const isAdmin = keycloak?.hasRealmRole("admin-role");

    return (
        <Routes>
            {/* Redirect root based on role */}
            <Route
                path="/"
                element={
                    <Navigate
                        to={isAdmin ? "/dashboard" : "/dashboard/products"}
                        replace
                    />
                }
            />

            {/* Protected Dashboard */}
            <Route path="/dashboard/*" element={<Dashboard/>}>
                {/* Home only for admin */}
                <Route
                    index
                    element={
                        <AdminRoute>
                            <Home/>
                        </AdminRoute>
                    }
                />

                {/* Products & Rentals for all role */}
                <Route path="products" element={<Products/>}/>
                <Route path="rentals" element={<Rentals/>}/>
                <Route path="request-rentals" element={<RequestRentals/>}/>
            </Route>

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace/>}/>
        </Routes>
    );
};


export default App;
