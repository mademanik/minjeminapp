import {Button, Card, Layout, theme} from "antd";
import Logo from "../components/Logo.jsx";
import MenuList from "../components/MenuList.jsx";
import ToggleThemeButton from "../components/ToggleThemeButton.jsx";
import {MenuFoldOutlined, MenuUnfoldOutlined} from '@ant-design/icons'
import {Content} from "antd/es/layout/layout.js";
import {Outlet} from "react-router-dom";
import {useState} from "react";

const {Header, Sider} = Layout;

const Dashboard = () => {
    const [darkTheme, setDarkTheme] = useState(true);
    const [collapsed, setCollapsed] = useState(false);

    const toggleTheme = () => {
        setDarkTheme(!darkTheme);
    }

    const {token: {colorBgContainer},} = theme.useToken();

    return (
        <Layout>
            <Sider collapsed={collapsed} collapsible trigger={null} theme={darkTheme ? 'dark' : 'light'} className="sidebar">
                <Logo/>
                <MenuList darkTheme={darkTheme}/>
                <ToggleThemeButton darkTheme={darkTheme} toggleTheme={toggleTheme}/>
            </Sider>
            <Layout>
                <Header style={{ padding: 0, background: colorBgContainer }}>
                    <Button type='text' className="toggle" onClick={() => setCollapsed(!collapsed)}
                            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />} />
                </Header>
                <Content style={{ padding: '24px', background: colorBgContainer }}>
                    <Card bordered={false} style={{ borderRadius: '8px', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)' }}>
                        <Outlet/>
                    </Card>
                </Content>
            </Layout>
        </Layout>
    )
};

export default Dashboard;