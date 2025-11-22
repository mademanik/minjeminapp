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
    const { keycloak, initialized } = useKeycloak();

    if (!initialized) {
        return (
            <div style={{ display: "flex", justifyContent: "center", marginTop: "20%" }}>
                <Spin size="large" tip="Initializing authentication..." />
            </div>
        );
    }

    const isLoggedIn = keycloak.authenticated;

    if (!isLoggedIn) {
        keycloak.login();
        return (
            <div style={{ display: "flex", justifyContent: "center", marginTop: "20%" }}>
                <Spin size="large" tip="Redirecting to login..." />
            </div>
        );
    }

    return (
        <Routes>
            {/* redirect root ke dashboard */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />

            {/* protected routes */}
            <Route path="/dashboard/*" element={<Dashboard />}>
                <Route index element={<Home />} />
                <Route path="products" element={<Products />} />
                <Route path="rentals" element={<Rentals />} />
            </Route>

            {/* fallback */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
    );
};


export default App;
